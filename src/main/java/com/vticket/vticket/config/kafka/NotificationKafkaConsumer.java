package com.vticket.vticket.config.kafka;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.response.PaymentResponse;
import com.vticket.vticket.service.MessageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private static final Logger logger = LogManager.getLogger(NotificationKafkaConsumer.class);

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserCollection userCollection;
    @Autowired
    private MessageService messageService;


    @KafkaListener(
            topics = KafkaConfig.TOPIC_EMAIL_TICKET,
            groupId = "vticket-notify-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void notifyTicketEvent(
            @Payload PaymentResponse payload,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        logger.info("NotificationKafkaConsumer|Received ticket event from partition={}, offset={}, bookingCode={}",
                partition, offset, payload.getBookingCode());

        try {
            if (payload.getUserId() == null) {
                logger.warn("NotificationKafkaConsumer|PaymentResponse userId is null");
                return;
            }

            User user = userCollection.getUserById(payload.getUserId());
            if (user == null) {
                logger.warn("NotificationKafkaConsumer|User not found for ID: {}", payload.getUserId());
                return;
            }

            System.out.println("NotificationKafkaConsumer|Preparing to send ticket email to: " + user.getEmail());


            logger.info("NotificationKafkaConsumer|Successfully sent ticket email to: {}", user.getEmail());
        } catch (Exception ex) {
            logger.error("NotificationKafkaConsumer|Error processing ticket email event: {}", payload, ex);
            throw ex;
        }
    }

}
