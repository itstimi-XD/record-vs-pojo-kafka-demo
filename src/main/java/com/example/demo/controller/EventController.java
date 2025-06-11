package com.example.demo.controller;

import com.example.demo.dto.pojo.UserEventPojoDto;
import com.example.demo.dto.record.UserEventRecordDto;
import com.example.demo.producer.PojoEventProducer;
import com.example.demo.producer.RecordEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 이벤트 전송을 위한 REST API 컨트롤러
 * 
 * 이 컨트롤러를 통해 Record와 POJO 기반 이벤트를 쉽게 테스트할 수 있습니다.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final RecordEventProducer recordEventProducer;
    private final PojoEventProducer pojoEventProducer;

    public EventController(RecordEventProducer recordEventProducer, PojoEventProducer pojoEventProducer) {
        this.recordEventProducer = recordEventProducer;
        this.pojoEventProducer = pojoEventProducer;
    }

    /**
     * Record 기반 이벤트 전송
     * 
     * POST /api/events/record
     * {
     *   "userId": "user123",
     *   "eventType": "LOGIN",
     *   "metadata": {"ip": "192.168.1.1", "userAgent": "Chrome"}
     * }
     */
    @PostMapping("/record")
    public ResponseEntity<String> sendRecordEvent(@RequestBody RecordEventRequest request) {
        try {
            UserEventRecordDto event = new UserEventRecordDto(
                    request.userId(),
                    request.eventType(),
                    LocalDateTime.now(),
                    request.metadata()
            );
            
            recordEventProducer.sendEvent(event);
            return ResponseEntity.ok("✅ Record event sent successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("❌ Failed to send Record event: " + e.getMessage());
        }
    }

    /**
     * POJO 기반 이벤트 전송
     * 
     * POST /api/events/pojo
     * {
     *   "userId": "user123",
     *   "eventType": "LOGIN",
     *   "metadata": {"ip": "192.168.1.1", "userAgent": "Chrome"}
     * }
     */
    @PostMapping("/pojo")
    public ResponseEntity<String> sendPojoEvent(@RequestBody PojoEventRequest request) {
        try {
            UserEventPojoDto event = new UserEventPojoDto(
                    request.getUserId(),
                    request.getEventType(),
                    LocalDateTime.now(),
                    request.getMetadata()
            );
            
            pojoEventProducer.sendEvent(event);
            return ResponseEntity.ok("✅ POJO event sent successfully");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("❌ Failed to send POJO event: " + e.getMessage());
        }
    }

    /**
     * 샘플 이벤트 생성 (테스트용)
     */
    @PostMapping("/sample")
    public ResponseEntity<String> sendSampleEvents() {
        try {
            // Record 샘플 이벤트
            UserEventRecordDto recordEvent = UserEventRecordDto.createNow(
                    "sample-user-record",
                    "LOGIN",
                    Map.of("source", "sample-api", "timestamp", System.currentTimeMillis())
            );
            recordEventProducer.sendEvent(recordEvent);

            // POJO 샘플 이벤트
            UserEventPojoDto pojoEvent = UserEventPojoDto.createNow(
                    "sample-user-pojo",
                    "LOGIN",
                    Map.of("source", "sample-api", "timestamp", System.currentTimeMillis())
            );
            pojoEventProducer.sendEvent(pojoEvent);

            return ResponseEntity.ok("✅ Sample events sent successfully (both Record and POJO)");
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("❌ Failed to send sample events: " + e.getMessage());
        }
    }

    /**
     * API 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "Event API is running",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    // Request DTOs
    public record RecordEventRequest(
            String userId,
            String eventType,
            Map<String, Object> metadata
    ) {}

    public static class PojoEventRequest {
        private String userId;
        private String eventType;
        private Map<String, Object> metadata;

        public PojoEventRequest() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}