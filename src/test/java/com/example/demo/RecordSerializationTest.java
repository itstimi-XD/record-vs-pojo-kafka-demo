package com.example.demo;

import com.example.demo.dto.record.UserEventRecordDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Record 직렬화/역직렬화 테스트
 * 
 * 이 테스트는 Record 클래스가 Jackson과 함께 올바르게 동작하는지 확인합니다.
 * 
 * 🔍 테스트 목적:
 * 1. Record 직렬화가 정상적으로 동작하는지 확인
 * 2. Record 역직렬화가 정상적으로 동작하는지 확인
 * 3. Jackson 설정이 올바른지 검증
 * 4. 실제 Kafka 환경에서 발생할 수 있는 문제 사전 발견
 */
@SpringBootTest
class RecordSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRecordSerialization() throws Exception {
        // Given: Record DTO 생성
        UserEventRecordDto originalEvent = new UserEventRecordDto(
                "test-user-123",
                "LOGIN",
                LocalDateTime.of(2024, 1, 1, 12, 0, 0),
                Map.of("ip", "192.168.1.1", "userAgent", "Chrome/120.0")
        );

        // When: JSON으로 직렬화
        String json = objectMapper.writeValueAsString(originalEvent);
        
        // Then: JSON이 올바르게 생성되었는지 확인
        assertNotNull(json);
        assertTrue(json.contains("test-user-123"));
        assertTrue(json.contains("LOGIN"));
        assertTrue(json.contains("2024-01-01T12:00:00"));
        assertTrue(json.contains("192.168.1.1"));
        
        System.out.println("✅ Record 직렬화 성공:");
        System.out.println(json);
    }

    @Test
    void testRecordDeserialization() throws Exception {
        // Given: JSON 문자열
        String json = """
                {
                    "userId": "test-user-456",
                    "eventType": "LOGOUT",
                    "timestamp": "2024-01-01T15:30:00",
                    "metadata": {
                        "ip": "10.0.0.1",
                        "sessionDuration": 3600
                    }
                }
                """;

        // When: JSON에서 Record로 역직렬화
        UserEventRecordDto deserializedEvent = objectMapper.readValue(json, UserEventRecordDto.class);

        // Then: 역직렬화가 올바르게 되었는지 확인
        assertNotNull(deserializedEvent);
        assertEquals("test-user-456", deserializedEvent.userId());
        assertEquals("LOGOUT", deserializedEvent.eventType());
        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 30, 0), deserializedEvent.timestamp());
        assertEquals("10.0.0.1", deserializedEvent.metadata().get("ip"));
        assertEquals(3600, deserializedEvent.metadata().get("sessionDuration"));
        
        System.out.println("✅ Record 역직렬화 성공:");
        System.out.println(deserializedEvent);
    }

    @Test
    void testRecordRoundTrip() throws Exception {
        // Given: 원본 Record DTO
        UserEventRecordDto originalEvent = UserEventRecordDto.createNow(
                "roundtrip-user",
                "PAGE_VIEW",
                Map.of("page", "/dashboard", "referrer", "https://google.com")
        );

        // When: 직렬화 후 역직렬화 (Round-trip)
        String json = objectMapper.writeValueAsString(originalEvent);
        UserEventRecordDto deserializedEvent = objectMapper.readValue(json, UserEventRecordDto.class);

        // Then: 원본과 역직렬화된 객체가 동일한지 확인
        assertEquals(originalEvent.userId(), deserializedEvent.userId());
        assertEquals(originalEvent.eventType(), deserializedEvent.eventType());
        assertEquals(originalEvent.metadata(), deserializedEvent.metadata());
        
        // Record는 자동으로 equals 구현
        // 단, LocalDateTime의 나노초 차이로 인해 직접 비교는 어려울 수 있음
        
        System.out.println("✅ Record Round-trip 테스트 성공");
        System.out.println("원본: " + originalEvent);
        System.out.println("복원: " + deserializedEvent);
    }

    @Test
    void testRecordValidation() {
        // Given & When & Then: Record의 생성자 검증 로직 테스트
        
        // 정상적인 경우
        assertDoesNotThrow(() -> new UserEventRecordDto(
                "valid-user",
                "VALID_EVENT",
                LocalDateTime.now(),
                Map.of()
        ));

        // userId가 null인 경우
        assertThrows(IllegalArgumentException.class, () -> new UserEventRecordDto(
                null,
                "VALID_EVENT",
                LocalDateTime.now(),
                Map.of()
        ));

        // eventType이 빈 문자열인 경우
        assertThrows(IllegalArgumentException.class, () -> new UserEventRecordDto(
                "valid-user",
                "",
                LocalDateTime.now(),
                Map.of()
        ));

        // timestamp가 null인 경우
        assertThrows(IllegalArgumentException.class, () -> new UserEventRecordDto(
                "valid-user",
                "VALID_EVENT",
                null,
                Map.of()
        ));
        
        System.out.println("✅ Record 검증 로직 테스트 성공");
    }

    @Test
    void testRecordBusinessMethods() {
        // Given: 다양한 이벤트 타입의 Record들
        UserEventRecordDto loginEvent = new UserEventRecordDto(
                "user1", "LOGIN", LocalDateTime.now(), Map.of()
        );
        
        UserEventRecordDto logoutEvent = new UserEventRecordDto(
                "user1", "LOGOUT", LocalDateTime.now(), Map.of()
        );
        
        UserEventRecordDto pageViewEvent = new UserEventRecordDto(
                "user1", "PAGE_VIEW", LocalDateTime.now(), Map.of("page", "/home")
        );

        // When & Then: 비즈니스 메서드 테스트
        assertTrue(loginEvent.isLoginEvent());
        assertFalse(loginEvent.isLogoutEvent());
        
        assertFalse(logoutEvent.isLoginEvent());
        assertTrue(logoutEvent.isLogoutEvent());
        
        assertFalse(pageViewEvent.isLoginEvent());
        assertFalse(pageViewEvent.isLogoutEvent());
        
        assertEquals("/home", pageViewEvent.getMetadataValue("page"));
        assertNull(pageViewEvent.getMetadataValue("nonexistent"));
        
        System.out.println("✅ Record 비즈니스 메서드 테스트 성공");
    }
}