package com.vticket.vticket.service;

import com.vticket.vticket.config.redis.RedisKey;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import static com.vticket.vticket.utils.CommonUtils.gson;
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
    @Autowired
    private UserCollection userCollection;

    public boolean sendRegistrationOtp(User user) {
        long start = System.currentTimeMillis();
        String otp = generateOtp();
        String emailKey = user.getEmail() == null ? null : user.getEmail().trim().toLowerCase();
        String key = String.format(RedisKey.OTP_EMAIL, emailKey);

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
        String emailKey = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String key = String.format(RedisKey.OTP_EMAIL, emailKey);

        try {
            String cachedOtp = redisService.getRedisSsoUser().opsForValue().get(key);
            logger.info("Retrieved OTP from Redis for email {} and otp {} with time {}ms", request.getEmail(),cachedOtp, (System.currentTimeMillis() - start));
            if (StringUtils.isEmpty(cachedOtp)) {
                logger.warn("No OTP found or OTP expired for email {}", request.getEmail());
                return false; // OTP expired or not found
            }

            if (request.getOtp() != null && request.getOtp().equals(cachedOtp)) {
                // fetch pending user by full email
                String pendingKey = String.format(RedisKey.PENDING_USER_EMAIL, emailKey);
                String pendingUserJson = redisService.getRedisSsoUser().opsForValue().get(pendingKey);
                if (StringUtils.isEmpty(pendingUserJson)) {
                    logger.warn("No pending user found for email {} despite valid OTP", request.getEmail());
                    return false;
                }
                User user = gson.fromJson(pendingUserJson, User.class);
                // insert user to mongo
                User savedUser = userCollection.insertUser(user);
                // cache user info directly and cleanup keys
                String userKey = RedisKey.USER_ID + savedUser.getId();
                redisService.getRedisSsoUser().opsForValue().set(userKey, gson.toJson(savedUser));
                redisService.getRedisSsoUser().expire(userKey, 30L, TimeUnit.MINUTES);//30p
                // cleanup pending and otp key
                redisService.getRedisSsoUser().delete(pendingKey);
                redisService.getRedisSsoUser().delete(key);
                logger.info("OTP verified and user inserted for email {} with userId {}", request.getEmail(), savedUser.getId());
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


