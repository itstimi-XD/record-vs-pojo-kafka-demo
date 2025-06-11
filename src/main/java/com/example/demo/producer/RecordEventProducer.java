package com.example.demo.producer;

import com.example.demo.dto.record.UserEventRecordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Record DTO를 사용한 Kafka 프로듀서
 * 
 * 이 클래스는 Record 기반 DTO를 Kafka로 전송하는 역할을 합니다.
 * Jackson의 Record 지원이 제대로 설정되어 있다면 정상적으로 직렬화됩니다.
 */
@Service
public class RecordEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(RecordEventProducer.class);

    private final KafkaTemplate<String, UserEventRecordDto> kafkaTemplate;
    private final String topicName;

    public RecordEventProducer(
            KafkaTemplate<String, UserEventRecordDto> kafkaTemplate,
            @Value("${demo.kafka.topics.record-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Record DTO를 Kafka로 전송
     * 
     * @param event 전송할 이벤트
     * @return 전송 결과를 담은 CompletableFuture
     */
    public CompletableFuture<SendResult<String, UserEventRecordDto>> sendEvent(UserEventRecordDto event) {
        logger.info("🚀 Sending Record event: {}", event);
        
        return kafkaTemplate.send(topicName, event.userId(), event)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("❌ Failed to send Record event: {}", event, throwable);
                    } else {
                        logger.info("✅ Successfully sent Record event to partition {} with offset {}",
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * 여러 이벤트를 배치로 전송
     */
    public void sendEvents(UserEventRecordDto... events) {
        for (UserEventRecordDto event : events) {
            sendEvent(event);
        }
    }
}