package com.example.demo.consumer;

import com.example.demo.dto.pojo.UserEventPojoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * POJO DTO를 사용한 Kafka 컨슈머
 * 
 * 이 클래스는 전통적인 POJO 기반 DTO를 Kafka에서 수신하는 역할을 합니다.
 * POJO는 Jackson과 완벽하게 호환되므로 별도 설정 없이도 정상 동작합니다.
 * 
 * ✅ 장점:
 * - 모든 Jackson 버전에서 안정적으로 동작
 * - 별도 설정 불필요
 * - 레거시 시스템과 호환성 우수
 */
@Service
public class PojoEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PojoEventConsumer.class);

    @KafkaListener(topics = "${demo.kafka.topics.pojo-events}", groupId = "pojo-consumer-group")
    public void consumePojoEvent(
            @Payload UserEventPojoDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("📨 Received POJO event from topic: {}, partition: {}, offset: {}", topic, partition, offset);
        logger.info("📋 Event details: {}", event);
        
        try {
            // 비즈니스 로직 처리
            processEvent(event);
            logger.info("✅ Successfully processed POJO event for user: {}", event.getUserId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to process POJO event: {}", event, e);
            // 실제 환경에서는 DLQ(Dead Letter Queue)로 전송하거나 재시도 로직 구현
        }
    }

    /**
     * 이벤트 처리 비즈니스 로직
     */
    private void processEvent(UserEventPojoDto event) {
        logger.debug("Processing {} event for user {}", event.getEventType(), event.getUserId());
        
        // POJO의 편의 메서드 활용
        if (event.isLoginEvent()) {
            logger.info("🔐 User {} logged in at {}", event.getUserId(), event.getTimestamp());
        } else if (event.isLogoutEvent()) {
            logger.info("🚪 User {} logged out at {}", event.getUserId(), event.getTimestamp());
        }
        
        // 메타데이터 처리
        if (event.getMetadata() != null && !event.getMetadata().isEmpty()) {
            logger.debug("📊 Event metadata: {}", event.getMetadata());
        }
        
        // 여기에 실제 비즈니스 로직 구현
        // 예: 데이터베이스 저장, 외부 API 호출, 알림 발송 등
    }
}