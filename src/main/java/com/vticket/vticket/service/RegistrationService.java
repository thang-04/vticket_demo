package com.vticket.vticket.service;

import com.vticket.vticket.dto.request.UserCreationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class RegistrationService {

    private static final Logger logger = LogManager.getLogger(RegistrationService.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private EmailService emailService;

    public void sendRegistrationOtp(UserCreationRequest request) {
        String otp = generateOtp();
        emailService.sendOtp(request.getEmail(), otp);
        logger.info("OTP generated and sent for email {}", request.getEmail());
    }

    private String generateOtp() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}


