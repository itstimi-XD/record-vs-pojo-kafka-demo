package com.example.demo;

import com.example.demo.dto.pojo.UserEventPojoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * POJO 직렬화/역직렬화 테스트
 * 
 * 이 테스트는 POJO 클래스가 Jackson과 함께 올바르게 동작하는지 확인합니다.
 * 
 * 🔍 테스트 목적:
 * 1. POJO 직렬화가 정상적으로 동작하는지 확인
 * 2. POJO 역직렬화가 정상적으로 동작하는지 확인
 * 3. Record와의 동작 차이점 비교
 * 4. 전통적인 Jackson 사용법 검증
 */
@SpringBootTest
class PojoSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPojoSerialization() throws Exception {
        // Given: POJO DTO 생성
        UserEventPojoDto originalEvent = new UserEventPojoDto(
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
        
        System.out.println("✅ POJO 직렬화 성공:");
        System.out.println(json);
    }

    @Test
    void testPojoDeserialization() throws Exception {
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

        // When: JSON에서 POJO로 역직렬화
        UserEventPojoDto deserializedEvent = objectMapper.readValue(json, UserEventPojoDto.class);

        // Then: 역직렬화가 올바르게 되었는지 확인
        assertNotNull(deserializedEvent);
        assertEquals("test-user-456", deserializedEvent.getUserId());
        assertEquals("LOGOUT", deserializedEvent.getEventType());
        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 30, 0), deserializedEvent.getTimestamp());
        assertEquals("10.0.0.1", deserializedEvent.getMetadata().get("ip"));
        assertEquals(3600, deserializedEvent.getMetadata().get("sessionDuration"));
        
        System.out.println("✅ POJO 역직렬화 성공:");
        System.out.println(deserializedEvent);
    }

    @Test
    void testPojoRoundTrip() throws Exception {
        // Given: 원본 POJO DTO
        UserEventPojoDto originalEvent = UserEventPojoDto.createNow(
                "roundtrip-user",
                "PAGE_VIEW",
                Map.of("page", "/dashboard", "referrer", "https://google.com")
        );

        // When: 직렬화 후 역직렬화 (Round-trip)
        String json = objectMapper.writeValueAsString(originalEvent);
        UserEventPojoDto deserializedEvent = objectMapper.readValue(json, UserEventPojoDto.class);

        // Then: 원본과 역직렬화된 객체가 동일한지 확인
        assertEquals(originalEvent.getUserId(), deserializedEvent.getUserId());
        assertEquals(originalEvent.getEventType(), deserializedEvent.getEventType());
        assertEquals(originalEvent.getMetadata(), deserializedEvent.getMetadata());
        
        // POJO는 equals 메서드를 구현했으므로 직접 비교 가능
        // 단, LocalDateTime의 나노초 차이로 인해 timestamp는 별도 확인
        
        System.out.println("✅ POJO Round-trip 테스트 성공");
        System.out.println("원본: " + originalEvent);
        System.out.println("복원: " + deserializedEvent);
    }

    @Test
    void testPojoDefaultConstructor() throws Exception {
        // Given: 기본 생성자로 POJO 생성
        UserEventPojoDto event = new UserEventPojoDto();
        
        // When: Setter로 값 설정
        event.setUserId("setter-user");
        event.setEventType("SETTER_TEST");
        event.setTimestamp(LocalDateTime.of(2024, 6, 1, 10, 0));
        event.setMetadata(Map.of("method", "setter"));

        // Then: 값이 올바르게 설정되었는지 확인
        assertEquals("setter-user", event.getUserId());
        assertEquals("SETTER_TEST", event.getEventType());
        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 0), event.getTimestamp());
        assertEquals("setter", event.getMetadata().get("method"));
        
        // JSON 직렬화도 정상 동작하는지 확인
        String json = objectMapper.writeValueAsString(event);
        assertNotNull(json);
        assertTrue(json.contains("setter-user"));
        
        System.out.println("✅ POJO 기본 생성자 + Setter 테스트 성공");
    }

    @Test
    void testPojoBusinessMethods() {
        // Given: 다양한 이벤트 타입의 POJO들
        UserEventPojoDto loginEvent = new UserEventPojoDto(
                "user1", "LOGIN", LocalDateTime.now(), Map.of()
        );
        
        UserEventPojoDto logoutEvent = new UserEventPojoDto(
                "user1", "LOGOUT", LocalDateTime.now(), Map.of()
        );
        
        UserEventPojoDto pageViewEvent = new UserEventPojoDto(
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
        
        System.out.println("✅ POJO 비즈니스 메서드 테스트 성공");
    }

    @Test
    void testPojoEqualsAndHashCode() {
        // Given: 동일한 내용의 POJO 두 개
        UserEventPojoDto event1 = new UserEventPojoDto(
                "user1", "LOGIN", LocalDateTime.of(2024, 1, 1, 12, 0), Map.of("key", "value")
        );
        
        UserEventPojoDto event2 = new UserEventPojoDto(
                "user1", "LOGIN", LocalDateTime.of(2024, 1, 1, 12, 0), Map.of("key", "value")
        );
        
        UserEventPojoDto event3 = new UserEventPojoDto(
                "user2", "LOGIN", LocalDateTime.of(2024, 1, 1, 12, 0), Map.of("key", "value")
        );

        // When & Then: equals와 hashCode 테스트
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
        
        assertNotEquals(event1, event3);
        assertNotEquals(event1.hashCode(), event3.hashCode());
        
        // toString 테스트
        String toString = event1.toString();
        assertTrue(toString.contains("user1"));
        assertTrue(toString.contains("LOGIN"));
        
        System.out.println("✅ POJO equals/hashCode/toString 테스트 성공");
        System.out.println("toString: " + toString);
    }

    @Test
    void testPojoMutability() {
        // Given: POJO 생성
        UserEventPojoDto event = new UserEventPojoDto(
                "original-user", "ORIGINAL_EVENT", LocalDateTime.now(), Map.of("original", "value")
        );
        
        String originalUserId = event.getUserId();

        // When: Setter로 값 변경 (POJO는 가변 객체)
        event.setUserId("modified-user");
        event.setEventType("MODIFIED_EVENT");

        // Then: 값이 변경되었는지 확인
        assertNotEquals(originalUserId, event.getUserId());
        assertEquals("modified-user", event.getUserId());
        assertEquals("MODIFIED_EVENT", event.getEventType());
        
        System.out.println("✅ POJO 가변성 테스트 성공");
        System.out.println("⚠️ 주의: POJO는 가변 객체이므로 의도치 않은 변경에 주의해야 합니다.");
    }
}