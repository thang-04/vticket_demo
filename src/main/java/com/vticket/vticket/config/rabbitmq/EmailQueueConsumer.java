package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.message.LoginEventMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.vticket.vticket.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueConsumer {
    private static final Logger logger = LogManager.getLogger(EmailQueueConsumer.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserCollection userCollection;
    @Autowired
    private MessageService messageService;

    //Auto listener message from queue
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME + "1")
    public void receiveEmailMessage(LoginEventMessage payload) {
        // Query user info từ DB
        User user = userCollection.getUserById(payload.getUserId());

        if (user == null) {
            logger.info("User not found for ID: " + payload.getUserId());
            return;
        }

        String subject = messageService.get("email.welcome.subject", user.getFull_name());
        String body = messageService.get("email.welcome.body", user.getFull_name(), payload.getLoginTime());

        // send email
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(user.getEmail());
        mail.setSubject(subject);
        mail.setText(body);
        mailSender.send(mail);

        System.out.println("Email sent to: " + user.getEmail());
    }
}
