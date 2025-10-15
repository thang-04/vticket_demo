package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.dto.message.LoginEventMessage;
import com.vticket.vticket.dto.response.PaymentResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmailLoginToQueue(LoginEventMessage payload) {
        rabbitTemplate.convertAndSend(
                Config.RABBITMQ.QUEUE_MAIL,
                Config.RABBITMQ.ROUTING_MAIL,
                payload
        );
    }

    public void sendEmailTicketToQueue(PaymentResponse payload) {
        rabbitTemplate.convertAndSend(
                Config.RABBITMQ.EXCHANGE_MAIL_TICKET,
                Config.RABBITMQ.ROUTING_MAIL_TICKET,
                payload
        );
    }
}

