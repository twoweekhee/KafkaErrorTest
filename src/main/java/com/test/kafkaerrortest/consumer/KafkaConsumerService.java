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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {


    private final KafkaTemplate<String, MessageDto> kafkaTemplate;
    private final RestTemplate restTemplate;
    private static Process currentProcess;
    private static final String JAR_PATH = "/Users/twoweekhee/Desktop/test/NHServer/build/libs/NHServer-0.0.1-SNAPSHOT.jar";


    public void listen(MessageDto messageDto) {
//        if (!isProcessRunning(messageDto)) {
//            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", JAR_PATH);
//            try {
//                Process process = processBuilder.start();
//                // 프로세스 출력 로깅
//                new Thread(() -> {
//                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                        String line;
//                        while ((line = reader.readLine()) != null) {
//                            log.info("JAR output: " + line);
//                        }
//                    } catch (IOException e) {
//
//                        log.error("Error reading JAR output", e);
//                    }
//                }).start();
//
//                // 프로세스가 준비될 때까지 대기 (예: 10초)
//                Thread.sleep(10000);
//            } catch (IOException e) {
//                log.info("process cannot start");
//                e.setStackTrace(e.getStackTrace());
//            } catch (InterruptedException e) {
//                log.error("Wait interrupted", e);
//                Thread.currentThread().interrupt();
//            }
//        }
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost")
                .port(messageDto.getPort())
                .path("/api/message")
                .build().toUri();
        log.info(messageDto.getMessage());
        ResponseEntity<String> result = restTemplate.postForEntity(uri, messageDto.getMessage(), String.class);
    }

    private static boolean isProcessRunning(MessageDto messageDto) {
        if(currentProcess != null && currentProcess.isAlive()) {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + messageDto.getPort() + "/api"))
                    .GET()
                    .build();

            try {
                log.info("send data");
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;
            } catch (ConnectException e) {
                return false;
            } catch (Exception e) {
                log.warn("Error checking if port is in use", e);
                return false;
            }
        }
        return false;
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
