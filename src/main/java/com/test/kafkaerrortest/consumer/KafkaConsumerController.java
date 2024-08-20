package com.test.kafkaerrortest.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KafkaConsumerController {

    private final KafkaConsumerService kafkaConsumerService;
    private final Long TIMEOUT = 300_000L;

    @KafkaListener(topics = "normal-topic", groupId = "kafkaGroup1", errorHandler="kafkaErrorHandler")
    @SendTo("error-topic")
    public void listener(String message) {
        log.info("listener {} ", message);
        kafkaConsumerService.listen(message);
    }

    @KafkaListener(topics = "error-topic", groupId = "kafkaGroup2")
    public void errorListener(String message) {
        log.info("errorListener {} ", message);
        kafkaConsumerService.errorListen(message);
    }

}
