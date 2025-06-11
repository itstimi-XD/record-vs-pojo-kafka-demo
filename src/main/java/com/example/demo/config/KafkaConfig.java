package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 토픽 설정
 * 
 * 애플리케이션 시작 시 필요한 토픽들을 자동으로 생성합니다.
 */
@Configuration
public class KafkaConfig {

    @Value("${demo.kafka.topics.record-events}")
    private String recordEventsTopic;

    @Value("${demo.kafka.topics.pojo-events}")
    private String pojoEventsTopic;

    @Value("${demo.kafka.topics.comparison-events}")
    private String comparisonEventsTopic;

    @Bean
    public NewTopic recordEventsTopic() {
        return TopicBuilder.name(recordEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pojoEventsTopic() {
        return TopicBuilder.name(pojoEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic comparisonEventsTopic() {
        return TopicBuilder.name(comparisonEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}