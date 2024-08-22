package com.test.kafkaerrortest.consumer;

import com.test.kafkaerrortest.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
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
    public void listener(@Payload MessageDto messageDto) {
        log.info("listener {} ", messageDto);
        kafkaConsumerService.listen(messageDto);
    }

    @KafkaListener(topics = "error-topic", groupId = "kafkaGroup2")
    public void errorListener(@Payload MessageDto messageDto) {
        log.info("errorListener {} ", messageDto);
        kafkaConsumerService.errorListen(messageDto);
    }

}
