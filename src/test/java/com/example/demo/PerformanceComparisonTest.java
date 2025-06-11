package com.example.demo;

import com.example.demo.dto.pojo.UserEventPojoDto;
import com.example.demo.dto.record.UserEventRecordDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Record vs POJO 성능 비교 테스트
 * 
 * 이 테스트는 Record와 POJO의 성능 차이를 측정합니다.
 * 
 * 🔍 측정 항목:
 * 1. 직렬화 성능 (객체 → JSON)
 * 2. 역직렬화 성능 (JSON → 객체)
 * 3. 메모리 사용량
 * 4. 객체 생성 성능
 */
@SpringBootTest
class PerformanceComparisonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private static final int ITERATIONS = 10000;
    private static final int WARMUP_ITERATIONS = 1000;

    @Test
    void compareSerializationPerformance() throws Exception {
        System.out.println("🚀 직렬화 성능 비교 테스트 시작");
        
        // 테스트 데이터 준비
        UserEventRecordDto recordEvent = new UserEventRecordDto(
                "perf-test-user",
                "PERFORMANCE_TEST",
                LocalDateTime.now(),
                Map.of("iteration", "test", "type", "serialization")
        );
        
        UserEventPojoDto pojoEvent = new UserEventPojoDto(
                "perf-test-user",
                "PERFORMANCE_TEST",
                LocalDateTime.now(),
                Map.of("iteration", "test", "type", "serialization")
        );

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            objectMapper.writeValueAsString(recordEvent);
            objectMapper.writeValueAsString(pojoEvent);
        }

        // Record 직렬화 성능 측정
        long recordStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            objectMapper.writeValueAsString(recordEvent);
        }
        long recordSerializationTime = System.nanoTime() - recordStartTime;

        // POJO 직렬화 성능 측정
        long pojoStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            objectMapper.writeValueAsString(pojoEvent);
        }
        long pojoSerializationTime = System.nanoTime() - pojoStartTime;

        // 결과 출력
        double recordAvgMs = recordSerializationTime / 1_000_000.0 / ITERATIONS;
        double pojoAvgMs = pojoSerializationTime / 1_000_000.0 / ITERATIONS;
        
        System.out.printf("📊 직렬화 성능 결과 (%d회 평균):%n", ITERATIONS);
        System.out.printf("   Record: %.4f ms%n", recordAvgMs);
        System.out.printf("   POJO:   %.4f ms%n", pojoAvgMs);
        System.out.printf("   차이:    %.2f%% %s%n", 
                Math.abs(recordAvgMs - pojoAvgMs) / Math.min(recordAvgMs, pojoAvgMs) * 100,
                recordAvgMs < pojoAvgMs ? "(Record 승리)" : "(POJO 승리)");
    }

    @Test
    void compareDeserializationPerformance() throws Exception {
        System.out.println("🚀 역직렬화 성능 비교 테스트 시작");
        
        // 테스트 JSON 데이터
        String json = """
                {
                    "userId": "perf-test-user",
                    "eventType": "PERFORMANCE_TEST",
                    "timestamp": "2024-01-01T12:00:00",
                    "metadata": {
                        "iteration": "test",
                        "type": "deserialization",
                        "number": 12345
                    }
                }
                """;

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            objectMapper.readValue(json, UserEventRecordDto.class);
            objectMapper.readValue(json, UserEventPojoDto.class);
        }

        // Record 역직렬화 성능 측정
        long recordStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            objectMapper.readValue(json, UserEventRecordDto.class);
        }
        long recordDeserializationTime = System.nanoTime() - recordStartTime;

        // POJO 역직렬화 성능 측정
        long pojoStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            objectMapper.readValue(json, UserEventPojoDto.class);
        }
        long pojoDeserializationTime = System.nanoTime() - pojoStartTime;

        // 결과 출력
        double recordAvgMs = recordDeserializationTime / 1_000_000.0 / ITERATIONS;
        double pojoAvgMs = pojoDeserializationTime / 1_000_000.0 / ITERATIONS;
        
        System.out.printf("📊 역직렬화 성능 결과 (%d회 평균):%n", ITERATIONS);
        System.out.printf("   Record: %.4f ms%n", recordAvgMs);
        System.out.printf("   POJO:   %.4f ms%n", pojoAvgMs);
        System.out.printf("   차이:    %.2f%% %s%n", 
                Math.abs(recordAvgMs - pojoAvgMs) / Math.min(recordAvgMs, pojoAvgMs) * 100,
                recordAvgMs < pojoAvgMs ? "(Record 승리)" : "(POJO 승리)");
    }

    @Test
    void compareObjectCreationPerformance() {
        System.out.println("🚀 객체 생성 성능 비교 테스트 시작");
        
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, Object> metadata = Map.of("test", "performance", "number", 42);

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            new UserEventRecordDto("user" + i, "EVENT", timestamp, metadata);
            new UserEventPojoDto("user" + i, "EVENT", timestamp, metadata);
        }

        // Record 객체 생성 성능 측정
        long recordStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            new UserEventRecordDto("user" + i, "EVENT", timestamp, metadata);
        }
        long recordCreationTime = System.nanoTime() - recordStartTime;

        // POJO 객체 생성 성능 측정
        long pojoStartTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            new UserEventPojoDto("user" + i, "EVENT", timestamp, metadata);
        }
        long pojoCreationTime = System.nanoTime() - pojoStartTime;

        // 결과 출력
        double recordAvgNs = (double) recordCreationTime / ITERATIONS;
        double pojoAvgNs = (double) pojoCreationTime / ITERATIONS;
        
        System.out.printf("📊 객체 생성 성능 결과 (%d회 평균):%n", ITERATIONS);
        System.out.printf("   Record: %.2f ns%n", recordAvgNs);
        System.out.printf("   POJO:   %.2f ns%n", pojoAvgNs);
        System.out.printf("   차이:    %.2f%% %s%n", 
                Math.abs(recordAvgNs - pojoAvgNs) / Math.min(recordAvgNs, pojoAvgNs) * 100,
                recordAvgNs < pojoAvgNs ? "(Record 승리)" : "(POJO 승리)");
    }

    @Test
    void compareMemoryUsage() {
        System.out.println("🚀 메모리 사용량 비교 테스트 시작");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 가비지 컬렉션 실행
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Record 객체들 생성
        UserEventRecordDto[] recordEvents = new UserEventRecordDto[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            recordEvents[i] = new UserEventRecordDto(
                    "user" + i,
                    "EVENT_" + i,
                    LocalDateTime.now(),
                    Map.of("index", i, "type", "record")
            );
        }
        
        long recordMemory = runtime.totalMemory() - runtime.freeMemory() - initialMemory;
        
        // 메모리 정리
        recordEvents = null;
        System.gc();
        Thread.yield(); // GC가 실행될 시간을 줌
        
        long afterRecordCleanup = runtime.totalMemory() - runtime.freeMemory();
        
        // POJO 객체들 생성
        UserEventPojoDto[] pojoEvents = new UserEventPojoDto[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            pojoEvents[i] = new UserEventPojoDto(
                    "user" + i,
                    "EVENT_" + i,
                    LocalDateTime.now(),
                    Map.of("index", i, "type", "pojo")
            );
        }
        
        long pojoMemory = runtime.totalMemory() - runtime.freeMemory() - afterRecordCleanup;
        
        // 결과 출력
        System.out.printf("📊 메모리 사용량 결과 (%d개 객체):%n", ITERATIONS);
        System.out.printf("   Record: %,d bytes (%.2f KB)%n", recordMemory, recordMemory / 1024.0);
        System.out.printf("   POJO:   %,d bytes (%.2f KB)%n", pojoMemory, pojoMemory / 1024.0);
        
        if (recordMemory != 0 && pojoMemory != 0) {
            double difference = Math.abs(recordMemory - pojoMemory) / (double) Math.min(recordMemory, pojoMemory) * 100;
            System.out.printf("   차이:    %.2f%% %s%n", 
                    difference,
                    recordMemory < pojoMemory ? "(Record 승리)" : "(POJO 승리)");
        }
        
        // 정리
        pojoEvents = null;
        System.gc();
    }

    @Test
    void printSummaryAndRecommendations() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 Record vs POJO 성능 비교 요약 및 권장사항");
        System.out.println("=".repeat(80));
        
        System.out.println("\n🎯 일반적인 성능 특성:");
        System.out.println("   • Record: 메모리 효율적, 불변성으로 인한 안전성");
        System.out.println("   • POJO:   Jackson과의 완벽한 호환성, 안정적인 성능");
        
        System.out.println("\n✅ Record 사용 권장 상황:");
        System.out.println("   • Java 17+ 환경");
        System.out.println("   • Jackson 2.12+ 사용");
        System.out.println("   • 불변성이 중요한 도메인 모델");
        System.out.println("   • 코드 간결성을 중시하는 프로젝트");
        
        System.out.println("\n✅ POJO 사용 권장 상황:");
        System.out.println("   • 레거시 환경 (Java 8~16, 오래된 Jackson)");
        System.out.println("   • 안정성이 최우선인 프로덕션 환경");
        System.out.println("   • 외부 시스템과의 호환성이 중요한 경우");
        System.out.println("   • 팀의 Record 경험이 부족한 경우");
        
        System.out.println("\n⚠️ 주의사항:");
        System.out.println("   • Record 사용 시 Jackson 설정 필수 확인");
        System.out.println("   • 성능 차이는 일반적으로 미미함");
        System.out.println("   • 비즈니스 요구사항과 팀 상황을 우선 고려");
        
        System.out.println("\n" + "=".repeat(80));
    }
}