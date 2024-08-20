# KafkaErrorTest


![스크린샷 2024-08-20 오후 2.21.01.png](https://prod-files-secure.s3.us-west-2.amazonaws.com/871daf03-d8e3-4021-9357-23bb53257469/0d17b895-04ec-4bdb-88fa-8a19f2a1fe1f/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2024-08-20_%E1%84%8B%E1%85%A9%E1%84%92%E1%85%AE_2.21.01.png)

```java
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3001")
public class KafkaConsumerController {

    private final KafkaConsumerService kafkaConsumerService;
    private final Long TIMEOUT = 300_000L;

    @KafkaListener(topics = "normal-topic", groupId = "kafkaGroup1", errorHandler = "kafkaErrorHandler")
    @SendTo("error-topic")
    public void listener(String message) {
        log.info("listener {} ", message);
        kafkaConsumerService.listen(message);
    }

    @KafkaListener(topics = "error-topic", groupId = "kafkaGroup2" , errorHandler = "kafkaErrorHandler")
    @SendTo("error-topic")
    public void errorListener(String message) {
        log.info("errorListener {} ", message);
        kafkaConsumerService.errorListen(message);
    }

}

```

```java
 @Bean
    public KafkaListenerErrorHandler kafkaErrorHandler() {
        return (m, e) -> {
            try {
                // m.getPayload()를 JsonNode로 파싱
                JsonNode jsonNode = objectMapper.readTree(m.getPayload().toString());

                // "message" 필드의 값 추출
                String message = jsonNode.path("message").asText();

                // 추출된 message 값 로그로 출력
                log.error("[KafkaErrorHandler] Extracted message=[" + message + "], errorMessage=[" + e.getMessage() + "]");

                // 이후 처리할 메시지를 반환
                return message;  // message 값만 반환하여 sendTo 토픽으로 전송
            } catch (Exception ex) {
                log.error("[KafkaErrorHandler] Failed to parse JSON payload, error: " + ex.getMessage());
                return m.getPayload();  // 파싱 실패 시 원본 payload 반환
            }
        };
    }
```
