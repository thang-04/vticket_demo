package com.vticket.vticket.config.kafka;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import com.vticket.vticket.service.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailKafkaConsumer {
    private static final Logger logger = LogManager.getLogger(EmailKafkaConsumer.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserCollection userCollection;
    @Autowired
    private MessageService messageService;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_EMAIL_LOGIN,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeLoginEvent(
            @Payload LoginEventMessage payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("Received login event from partition={}, offset={}, userId={}",
                partition, offset, payload.getUserId());

        try {
            User user = userCollection.getUserById(payload.getUserId());

            if (user == null) {
                logger.warn("User not found for ID: {}", payload.getUserId());
                return;
            }

            String subject = messageService.get("email.welcome.subject", user.getFull_name());
            String body = messageService.get("email.welcome.body",
                    user.getFull_name(), payload.getLoginTime());

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            logger.info("Successfully sent login email to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("Error processing login email event for userId: {}",
                    payload.getUserId(), ex);
            // Kafka will handle retry if an exception
            throw ex;
        }
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_EMAIL_TICKET,
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTicketEvent(
            @Payload PaymentResponse payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("consumeTicketEvent|Received ticket event from partition={}, offset={}, bookingCode={}",
                partition, offset, payload.getBookingCode());

        try {
            if (payload.getUserId() == null) {
                logger.warn("consumeTicketEvent|PaymentResponse userId is null");
                return;
            }

            User user = userCollection.getUserById(payload.getUserId());
            if (user == null) {
                logger.warn("consumeTicketEvent|User not found for ID: {}", payload.getUserId());
                return;
            }

            String subject = messageService.get("email.ticket.subject", user.getFull_name());
            String body = messageService.get("email.ticket.body",
                    user.getFull_name(),
                    payload.getEventId(),
                    payload.getBookingCode(),
                    payload.getTotalAmount()
            );

            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            System.out.println("consumeTicketEvent|Successfully sent ticket email to: " + user.getEmail());

            logger.info("consumeTicketEvent|Successfully sent ticket email to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("consumeTicketEvent|Error processing ticket email event: {}", payload, ex);
            throw ex;
        }
    }
}