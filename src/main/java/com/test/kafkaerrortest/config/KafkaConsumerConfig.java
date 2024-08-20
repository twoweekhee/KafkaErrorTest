package com.test.kafkaerrortest.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;


import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String BOOTSTRAP_SERVERS;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String OFFSET_RESET;

    @Value("${spring.kafka.consumer.group-id}")
    private String GROUP_ID;

    private final ObjectMapper objectMapper = new ObjectMapper();  // ObjectMapper 인스턴스 생성

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,OFFSET_RESET);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.consumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String,String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(this.consumerFactory());
        factory.setReplyTemplate(kafkaTemplate);
        return factory;
    }

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

    @Bean
    public KafkaListenerErrorHandler kafkaErrorHandlerSecond(){
        return (m, e) -> {
            try {
                // m.getPayload()를 JsonNode로 파싱
                JsonNode jsonNode = objectMapper.readTree(m.getPayload().toString());

                // "message" 필드의 값 추출
                String message = jsonNode.path("message").asText();

                // 추출된 message 값 로그로 출력
                log.error("[KafkaErrorHandler] Extracted message=[" + message + "], errorMessage=[" + e.getMessage() + "]");

                Thread.sleep(1000000);
                // 이후 처리할 메시지를 반환
                return message;  // message 값만 반환하여 sendTo 토픽으로 전송
            } catch (Exception ex) {
                log.error("[KafkaErrorHandler] Failed to parse JSON payload, error: " + ex.getMessage());
                return m.getPayload();  // 파싱 실패 시 원본 payload 반환
            }
        };
    }

}

