package com.test.kafkaerrortest.producer;

import com.test.kafkaerrortest.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class KafkaProducerController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/kafka")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody MessageDto messageDto) {
        kafkaProducerService.sendMessage(messageDto);
        return ResponseEntity.ok(messageDto);
    }
}