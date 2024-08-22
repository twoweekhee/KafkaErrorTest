package com.test.kafkaerrortest.consumer;

import com.test.kafkaerrortest.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {


    private final KafkaTemplate<String, MessageDto> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public void listen(MessageDto messageDto) {
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost")
                .port(messageDto.getPort())
                .path("/api/message")
                .build().toUri();
        ResponseEntity<String> result = restTemplate.postForEntity(uri, messageDto.getMessage(), String.class);
    }


    public void errorListen( MessageDto messageDto) {

        log.info("### error: " + messageDto);

        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost")
                .port(messageDto.getPort())
                .path("/api/message/error")
                .build().toUri();

        ResponseEntity<String> result = restTemplate.postForEntity(uri, messageDto.getMessage(), String.class);
    }

}
