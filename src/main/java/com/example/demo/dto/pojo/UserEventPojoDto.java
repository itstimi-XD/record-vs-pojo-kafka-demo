package com.example.demo.dto.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * POJO 기반 사용자 이벤트 DTO
 * 
 * ✅ POJO의 장점:
 * - Jackson과 완벽한 호환성 (기본 생성자 + setter)
 * - 모든 Java 버전에서 사용 가능
 * - 레거시 시스템과의 호환성
 * - 디버깅 용이성
 * - 런타임 안정성
 * 
 * ⚠️ POJO의 단점:
 * - Boilerplate 코드 많음 (getter, setter, equals, hashCode, toString)
 * - 불변성을 위해서는 추가 작업 필요
 * - 실수로 인한 버그 가능성 (setter 사용)
 * 
 * 🔧 Jackson 역직렬화 요구사항:
 * 1. 기본 생성자 (매개변수 없는 생성자)
 * 2. 각 필드에 대한 setter 메서드
 * 3. 선택적으로 getter 메서드 (직렬화용)
 */
public class UserEventPojoDto {
    
    private String userId;
    private String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    /**
     * 기본 생성자 - Jackson 역직렬화를 위해 필수!
     */
    public UserEventPojoDto() {
    }
    
    /**
     * 전체 매개변수 생성자 - 편의성을 위해 제공
     */
    public UserEventPojoDto(String userId, String eventType, LocalDateTime timestamp, Map<String, Object> metadata) {
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
    
    // Getter 메서드들
    public String getUserId() {
        return userId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    // Setter 메서드들 - Jackson 역직렬화를 위해 필수!
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * 편의 메서드: 현재 시간으로 이벤트 생성
     */
    public static UserEventPojoDto createNow(String userId, String eventType, Map<String, Object> metadata) {
        return new UserEventPojoDto(userId, eventType, LocalDateTime.now(), metadata);
    }
    
    /**
     * 편의 메서드: 메타데이터 없이 이벤트 생성
     */
    public static UserEventPojoDto createSimple(String userId, String eventType) {
        return new UserEventPojoDto(userId, eventType, LocalDateTime.now(), Map.of());
    }
    
    /**
     * 비즈니스 로직 메서드
     */
    public boolean isLoginEvent() {
        return "LOGIN".equals(eventType);
    }
    
    public boolean isLogoutEvent() {
        return "LOGOUT".equals(eventType);
    }
    
    /**
     * 메타데이터에서 특정 값 추출
     */
    public String getMetadataValue(String key) {
        return metadata != null ? String.valueOf(metadata.get(key)) : null;
    }
    
    /**
     * equals 메서드 - 객체 비교를 위해 필수
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEventPojoDto that = (UserEventPojoDto) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(metadata, that.metadata);
    }
    
    /**
     * hashCode 메서드 - 해시 기반 컬렉션 사용을 위해 필수
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, eventType, timestamp, metadata);
    }
    
    /**
     * toString 메서드 - 디버깅 및 로깅을 위해 유용
     */
    @Override
    public String toString() {
        return "UserEventPojoDto{" +
               "userId='" + userId + '\'' +
               ", eventType='" + eventType + '\'' +
               ", timestamp=" + timestamp +
               ", metadata=" + metadata +
               '}';
    }
}