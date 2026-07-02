package com.paycore.fraud.config;

import com.paycore.common.FraudEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean NewTopic fraudEvents() { return TopicBuilder.name("fraud-events").partitions(3).replicas(1).build(); }
    @Bean NewTopic fraudAlerts() { return TopicBuilder.name("fraud-alerts").partitions(3).replicas(1).build(); }
}
