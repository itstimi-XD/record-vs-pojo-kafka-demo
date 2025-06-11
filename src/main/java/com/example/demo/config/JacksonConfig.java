package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson 설정 클래스
 * 
 * ⚠️ 중요: Record 클래스의 역직렬화를 위해서는 다음 설정이 필수입니다:
 * 
 * 1. ParameterNamesModule 등록
 *    - Record의 생성자 매개변수 이름을 인식하기 위해 필요
 *    - 컴파일 시 -parameters 옵션과 함께 사용
 * 
 * 2. JavaTimeModule 등록
 *    - LocalDateTime 등 Java 8+ 시간 API 지원
 * 
 * 3. WRITE_DATES_AS_TIMESTAMPS 비활성화
 *    - ISO 8601 형식으로 날짜 직렬화
 * 
 * 이 설정 없이 Record를 사용하면 다음 예외가 발생합니다:
 * "Cannot construct instance of `RecordDto` (no Creators, like default constructor, exist)"
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 🔑 Record 지원을 위한 핵심 모듈
        mapper.registerModule(new ParameterNamesModule());
        
        // Java 8+ 시간 API 지원
        mapper.registerModule(new JavaTimeModule());
        
        // 날짜를 타임스탬프가 아닌 ISO 8601 형식으로 직렬화
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 알 수 없는 속성 무시 (선택사항)
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }
    
    /**
     * Record 지원 여부를 확인하는 유틸리티 메서드
     * 
     * @param mapper ObjectMapper 인스턴스
     * @return Record 지원 여부
     */
    public static boolean supportsRecords(ObjectMapper mapper) {
        return mapper.getRegisteredModuleIds().contains("jackson-module-parameter-names");
    }
}