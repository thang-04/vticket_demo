package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.config.Config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    //CONFIG EMAIL QUEUE
    @Bean
    public Queue emailQueue() {
        return new Queue(Config.RABBITMQ.QUEUE_MAIL, true); // durable = true để lưu queue nếu server restart
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(Config.RABBITMQ.EXCHANGE_MAIL);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(emailExchange)
                .with(Config.RABBITMQ.ROUTING_MAIL);
    }

    // Convert object Java sang JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //CONFIG TICKET QUEUE
//    @Bean
//    public Queue ticketQueue() {
//        return new Queue(Config.RABBITMQ.QUEUE_TICKET, true);
//    }
//
//    @Bean
//    public TopicExchange ticketExchange() {
//        return new TopicExchange(Config.RABBITMQ.EXCHANGE_TICKET);
//    }
//
//    @Bean
//    public Binding ticketBinding(Queue ticketQueue, TopicExchange ticketExchange) {
//        return BindingBuilder.bind(ticketQueue)
//                .to(ticketExchange)
//                .with(Config.RABBITMQ.ROUTING_TICKET);
//    }

}

