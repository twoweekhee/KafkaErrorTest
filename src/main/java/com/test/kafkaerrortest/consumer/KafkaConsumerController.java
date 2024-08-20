package com.test.kafkaerrortest.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
