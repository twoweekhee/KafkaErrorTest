package com.test.kafkaerrortest.consumer;

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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {


    private final KafkaTemplate<String,String> kafkaTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public void listen(String message) {
        String url = "http://localhost:8070/api/message";
        ResponseEntity<String> result = restTemplate.postForEntity(url, message, String.class);
    }

    public void errorListen(String message) {

        log.info("### error: " + message);

        String url = "http://localhost:8070/api/message/error";

        ResponseEntity<String> result = restTemplate.postForEntity(url, message, String.class);
    }

    public void retryListen(String message) {
        log.info("### retry: " + message);
        try {
            RetryTemplate retryTemplate = new RetryTemplate();
            String url = "http://localhost:8070/api/message/error";
            restTemplate.postForEntity(url, message, String.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
