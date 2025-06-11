package com.example.demo.consumer;

import com.example.demo.dto.record.UserEventRecordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Record DTO를 사용한 Kafka 컨슈머
 * 
 * 이 클래스는 Record 기반 DTO를 Kafka에서 수신하는 역할을 합니다.
 * Jackson의 Record 지원이 제대로 설정되어 있다면 정상적으로 역직렬화됩니다.
 * 
 * ⚠️ 주의사항:
 * - Jackson 2.12+ 필요
 * - jackson-module-parameter-names 의존성 필요
 * - 컴파일 시 -parameters 옵션 필요
 * - ParameterNamesModule 등록 필요
 */
@Service
public class RecordEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RecordEventConsumer.class);

    @KafkaListener(topics = "${demo.kafka.topics.record-events}", groupId = "record-consumer-group")
    public void consumeRecordEvent(
            @Payload UserEventRecordDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("📨 Received Record event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        logger.info("📋 Event details: {}", event);
        
        try {
            // 비즈니스 로직 처리
            processEvent(event);
            logger.info("✅ Successfully processed Record event for user: {}", event.userId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to process Record event: {}", event, e);
            // 실제 환경에서는 DLQ(Dead Letter Queue)로 전송하거나 재시도 로직 구현
        }
    }

    /**
     * 이벤트 처리 비즈니스 로직
     */
    private void processEvent(UserEventRecordDto event) {
        // Record의 불변성 활용
        logger.debug("Processing {} event for user {}", event.eventType(), event.userId());
        
        // Record의 편의 메서드 활용
        if (event.isLoginEvent()) {
            logger.info("🔐 User {} logged in at {}", event.userId(), event.timestamp());
        } else if (event.isLogoutEvent()) {
            logger.info("🚪 User {} logged out at {}", event.userId(), event.timestamp());
        }
        
        // 메타데이터 처리
        if (event.metadata() != null && !event.metadata().isEmpty()) {
            logger.debug("📊 Event metadata: {}", event.metadata());
        }
        
        // 여기에 실제 비즈니스 로직 구현
        // 예: 데이터베이스 저장, 외부 API 호출, 알림 발송 등
    }
}