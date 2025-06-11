package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Record vs POJO Kafka Demo Application
 * 
 * 이 애플리케이션은 Record 클래스와 POJO 클래스를 사용한 
 * Kafka 메시지 직렬화/역직렬화의 차이점을 시연합니다.
 * 
 * 주요 학습 포인트:
 * 1. Jackson의 Record 지원 설정
 * 2. Record vs POJO 성능 비교
 * 3. 역직렬화 이슈 해결 방법
 * 4. 실무 환경에서의 선택 기준
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        
        System.out.println("🚀 Record vs POJO Kafka Demo Application Started!");
        System.out.println("📊 Kafka UI: http://localhost:8080");
        System.out.println("🔧 Test endpoints: http://localhost:8081/api/events");
    }
}