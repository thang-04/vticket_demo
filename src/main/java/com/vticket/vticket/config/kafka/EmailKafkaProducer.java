package com.vticket.vticket.config.kafka;

import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class EmailKafkaProducer {
    private static final Logger logger = LogManager.getLogger(EmailKafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmailLoginEvent(LoginEventMessage payload) {
        // Send login email event to Kafka topic
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC_EMAIL_LOGIN, payload.getUserId(), payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent login email event for userId={} with offset={}",
                        payload.getUserId(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send login email event for userId={}",
                        payload.getUserId(), ex);
            }
        });
    }

    public void sendEmailTicketEvent(PaymentResponse payload) {
        // Send ticket email event to Kafka topic
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC_EMAIL_TICKET, payload.getUserId(), payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent ticket email event for bookingCode={} with offset={}",
                        payload.getBookingCode(), result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send ticket email event for bookingCode={}",
                        payload.getBookingCode(), ex);
            }
        });
    }
}
