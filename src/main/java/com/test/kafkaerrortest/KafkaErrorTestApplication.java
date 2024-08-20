package com.test.kafkaerrortest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KafkaErrorTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaErrorTestApplication.class, args);
    }

}
