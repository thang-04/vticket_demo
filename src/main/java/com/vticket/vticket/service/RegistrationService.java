package com.vticket.vticket.service;

import com.vticket.vticket.config.redis.RedisKey;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.OtpVerifyRequest;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class RegistrationService {

    private static final Logger logger = LogManager.getLogger(RegistrationService.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private EmailService emailService;
    @Autowired
    private RedisService redisService;

    public boolean sendRegistrationOtp(User user) {
        long start = System.currentTimeMillis();
        String otp = generateOtp();
        String localPart = user.getEmail().split("@")[0];
        String key = String.format(RedisKey.OTP_EMAIL, localPart);

        try {
            emailService.sendOtp(user.getEmail(), otp);
            logger.info("OTP generated and sent for email {} with time {}", user.getEmail(), (System.currentTimeMillis() - start));
            //cache redis
            redisService.getRedisSsoUser().opsForValue().set(key, otp);
            redisService.getRedisSsoUser().expire(key, 5L, TimeUnit.MINUTES); // 5p
            logger.info("Stored OTP in Redis for user ID {} with time {}", user.getId(), (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            logger.error("Failed to send OTP to email {}: {}", user.getEmail(), e.getMessage());
        }
        return false;
    }

    private String generateOtp() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    public boolean verifyOtp(OtpVerifyRequest request) {
        long start = System.currentTimeMillis();
        String localPart = request.getEmail().split("@")[0];
        String key = String.format(RedisKey.OTP_EMAIL, localPart);

        try {
            String cachedOtp = redisService.getRedisSsoUser().opsForValue().get(key);
            logger.info("Retrieved OTP from Redis for email {} and otp {} with time {}ms", request.getEmail(),cachedOtp, (System.currentTimeMillis() - start));
            if (StringUtils.isEmpty(cachedOtp)) {
                logger.warn("No OTP found or OTP expired for email {}", request.getEmail());
                return false; // OTP expired or not found
            }

            if (cachedOtp.equals(request.getOtp())) {
//                redisService.getRedisSsoUser().delete(key);
                logger.info("OTP verified successfully for email {}", request.getEmail());
                return true;
            } else {
                logger.warn("Invalid OTP for email {}", request.getEmail());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error verifying OTP for email {}: {}", request.getEmail(), e.getMessage());
            return false;
        }
    }
    public boolean resendRegistrationOtp(OtpVerifyRequest request) {
        long start = System.currentTimeMillis();
        String localPart = request.getEmail().split("@")[0];
        String key = String.format(RedisKey.OTP_EMAIL, localPart);

        try {
            String cachedOtp = redisService.getRedisSsoUser().opsForValue().get(key);
            String otp;

            if (StringUtils.isEmpty(cachedOtp)) {
                otp = generateOtp();
                redisService.getRedisSsoUser().opsForValue().set(key, otp);
                redisService.getRedisSsoUser().expire(key, 5L, TimeUnit.MINUTES);
                logger.info("Generated NEW OTP for email {} after expiration", request.getEmail());
            } else {
                otp = cachedOtp;
                logger.info("Reusing EXISTING OTP for email {} still valid", request.getEmail());
            }

            emailService.sendOtp(request.getEmail(), otp);
            logger.info("Re-sent OTP to email {} with time {}ms",
                    request.getEmail(), (System.currentTimeMillis() - start));
            return true;

        } catch (Exception e) {
            logger.error("Failed to re-send OTP to email {}: {}", request.getEmail(), e.getMessage());
            return false;
        }
    }

}


