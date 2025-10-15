package com.vticket.vticket.config.rabbitmq;

import com.vticket.vticket.config.Config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    //CONFIG QUEUE
    @Bean
    public Queue emailLoginQueue() {
        return new Queue(Config.RABBITMQ.QUEUE_MAIL, true); // durable = true để lưu queue nếu server restart
    }

    @Bean
    public Queue emailTicketQueue() {
        return new Queue(Config.RABBITMQ.QUEUE_MAIL_TICKET, true);
    }

    //CONFIG EXCHANGE
    @Bean
    public DirectExchange emailLoginExchange() {
        return new DirectExchange(Config.RABBITMQ.EXCHANGE_MAIL);
    }

    @Bean
    public DirectExchange emailTicketExchange() {
        return new DirectExchange(Config.RABBITMQ.EXCHANGE_MAIL_TICKET);
    }

    //CONFIG BINDING
    @Bean
    public Binding emailLoginBinding(Queue emailLoginQueue, DirectExchange emailLoginExchange) {
        return BindingBuilder.bind(emailLoginQueue)
                .to(emailLoginExchange)
                .with(Config.RABBITMQ.ROUTING_MAIL);
    }

    @Bean
    public Binding emailTicketBinding(Queue emailTicketQueue, DirectExchange emailTicketExchange) {
        return BindingBuilder.bind(emailTicketQueue)
                .to(emailTicketExchange)
                .with(Config.RABBITMQ.ROUTING_MAIL_TICKET);
    }

    // Convert object Java sang JSON
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}

