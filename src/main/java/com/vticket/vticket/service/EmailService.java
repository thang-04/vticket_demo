package com.vticket.vticket.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String mailFrom;

    public void sendOtp(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(mailFrom);
            message.setSubject("Your VTicket OTP Code");
            message.setText("Your OTP code is: " + otp + "\nThis code will expire in 5 minutes. If you did not request this, please ignore this email.");
            mailSender.send(message);
            logger.info("Sent OTP email to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {} - {}", toEmail, e.getMessage(), e);
        }
    }
}


