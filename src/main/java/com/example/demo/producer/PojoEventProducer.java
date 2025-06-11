package com.example.demo.producer;

import com.example.demo.dto.pojo.UserEventPojoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * POJO DTO를 사용한 Kafka 프로듀서
 * 
 * 이 클래스는 전통적인 POJO 기반 DTO를 Kafka로 전송하는 역할을 합니다.
 * POJO는 Jackson과 완벽하게 호환되므로 별도 설정 없이도 정상 동작합니다.
 */
@Service
public class PojoEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(PojoEventProducer.class);

    private final KafkaTemplate<String, UserEventPojoDto> kafkaTemplate;
    private final String topicName;

    public PojoEventProducer(
            KafkaTemplate<String, UserEventPojoDto> kafkaTemplate,
            @Value("${demo.kafka.topics.pojo-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * POJO DTO를 Kafka로 전송
     * 
     * @param event 전송할 이벤트
     * @return 전송 결과를 담은 CompletableFuture
     */
    public CompletableFuture<SendResult<String, UserEventPojoDto>> sendEvent(UserEventPojoDto event) {
        logger.info("🚀 Sending POJO event: {}", event);
        
        return kafkaTemplate.send(topicName, event.getUserId(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("❌ Failed to send POJO event: {}", event, throwable);
                    } else {
                        logger.info("✅ Successfully sent POJO event to partition {} with offset {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 여러 이벤트를 배치로 전송
     */
    public void sendEvents(UserEventPojoDto... events) {
        for (UserEventPojoDto event : events) {
            sendEvent(event);
        }
    }
}