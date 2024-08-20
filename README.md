# KafkaErrorTest

- 리스너에서는 레코드를 받았지만 받아서 서비스 로직에서 에러가 발생하여 정상처리가 안되었을 때
- errorHandler 사용 및 @SendTo
<br>

![스크린샷 2024-08-20 오후 2 26 40](https://github.com/user-attachments/assets/93a2db45-8fc6-4814-815d-a21ccd5e3020)
<br>

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
