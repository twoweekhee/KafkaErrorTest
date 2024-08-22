package com.test.kafkaerrortest.producer;

import com.test.kafkaerrortest.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    @Value("${spring.kafka.test-topic.name}")
    private String topicName;

    /* Kafka Template 을 이용해 Kafka Broker 전송 */

    private final KafkaTemplate<String, MessageDto> kafkaTemplate;

    public void sendMessage(MessageDto messageDto) {
        log.info("Producer Message : {}",messageDto.getPort());
        kafkaTemplate.send(topicName, messageDto);
    }
}
