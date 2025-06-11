package com.example.demo.dto.record;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Record 기반 사용자 이벤트 DTO
 * 
 * ✅ Record의 장점:
 * - 간결한 코드 (boilerplate 코드 최소화)
 * - 불변성 자동 보장 (모든 필드가 final)
 * - equals, hashCode, toString 자동 생성
 * - 컴파일 타임 안전성
 * 
 * ⚠️ Record의 주의사항:
 * - Jackson 역직렬화를 위해서는 특별한 설정 필요
 * - 기본 생성자가 없음 (모든 매개변수를 받는 생성자만 존재)
 * - Setter 메서드 없음 (불변 객체)
 * - Java 14+ 필요 (Java 17+에서 정식 지원)
 * 
 * 🔧 필수 설정:
 * 1. jackson-module-parameter-names 의존성
 * 2. 컴파일 시 -parameters 옵션
 * 3. ParameterNamesModule 등록
 */
public record UserEventRecordDto(
    String userId,
    String eventType,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    Map<String, Object> metadata
) {
    
    /**
     * Record에서도 생성자 검증 로직을 추가할 수 있습니다.
     * 이는 compact constructor라고 불립니다.
     */
    public UserEventRecordDto {
        // 검증 로직
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("eventType cannot be null or empty");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp cannot be null");
        }
        
        // 정규화 (필요시)
        userId = userId.trim();
        eventType = eventType.trim().toUpperCase();
    }
    
    /**
     * 편의 메서드: 현재 시간으로 이벤트 생성
     */
    public static UserEventRecordDto createNow(String userId, String eventType, Map<String, Object> metadata) {
        return new UserEventRecordDto(userId, eventType, LocalDateTime.now(), metadata);
    }
    
    /**
     * 편의 메서드: 메타데이터 없이 이벤트 생성
     */
    public static UserEventRecordDto createSimple(String userId, String eventType) {
        return new UserEventRecordDto(userId, eventType, LocalDateTime.now(), Map.of());
    }
    
    /**
     * 비즈니스 로직 메서드도 추가 가능
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
}