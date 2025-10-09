package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.dto.message.LoginEventMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailQueueProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmailToQueue(LoginEventMessage payload) {
        rabbitTemplate.convertAndSend(
                Config.RABBITMQ.QUEUE_MAIL,
                Config.RABBITMQ.ROUTING_MAIL,
                payload
        );
    }
}

