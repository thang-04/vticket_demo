package com.vticket.vticket.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_EMAIL_LOGIN = "email-login-events";
    public static final String TOPIC_EMAIL_TICKET = "email-ticket-events";

    @Bean
    public NewTopic emailLoginTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_LOGIN)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailTicketTopic() {
        return TopicBuilder.name(TOPIC_EMAIL_TICKET)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
