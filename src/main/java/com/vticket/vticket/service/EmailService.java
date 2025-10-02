package com.vticket.vticket.service;

import com.vticket.vticket.config.rabbitmq.EmailQueueProducer;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.message.LoginEventMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);

    @Value("${app.mail.from}")
    private String mailFrom;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailQueueProducer emailQueueProducer;


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


    public void sendWelcomeEmail(User newUser) {
        long start = System.currentTimeMillis();
        logger.info("Preparing to send welcome email to user ID: {} at {}", newUser.getId(), newUser.getEmail());

        LoginEventMessage payload = LoginEventMessage.builder()
                .userId(newUser.getId())
                .loginTime(new Date())
                .build();

        emailQueueProducer.sendEmailToQueue(payload);
        logger.info("Enqueued welcome email for user ID: {} in {} ms", newUser.getId(), (System.currentTimeMillis() - start));
    }


//    public void sendWelcomeEmail(User newUser) {
//        long start = System.currentTimeMillis();
//        logger.info("Preparing to send welcome email to user ID: {} at {}", newUser.getId(), newUser.getEmail());
//        LoginEventMessage payload = new LoginEventMessage(
//                newUser.getId(),
//                newUser.getCreated_at(),
//                newUser.getEmail(),
//                "Chào mừng " + newUser.getFull_name(),
//                "Cảm ơn bạn đã đăng ký tài khoản tại VTicket vào lúc "+newUser.getCreated_at()+
//                        "\n\nChúng tôi rất vui được chào đón bạn đến với cộng đồng của chúng tôi."+
//                        "\nHãy khám phá các sự kiện hấp dẫn và tận hưởng những trải nghiệm tuyệt vời cùng VTicket."+
//                        "\nNếu bạn có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi."+
//                        "\n\nChúc bạn có những trải nghiệm tuyệt vời!"+
//                        "\nTrân trọng,"+
//                        "\nĐội ngũ VTicket"
//        );
//        // send message to queue
//        emailQueueProducer.sendEmailToQueue(payload);
//        logger.info("Enqueued welcome email for user ID: {} in {} ms", newUser.getId(), (System.currentTimeMillis() - start));
//    }

}


