package com.test.kafkaerrortest.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.kafkaerrortest.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;


import java.io.IOException;
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

    private final KafkaTemplate<String, MessageDto> kafkaTemplate;

    @Bean
    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,OFFSET_RESET);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.test.kafkaerrortest.dto");
        props.put(JsonDeserializer.TYPE_MAPPINGS, "messageDto:com.test.kafkaerrortest.dto.MessageDto");

        return props;
    }
    @Bean
    public ConsumerFactory<String, MessageDto> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.consumerConfig());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,MessageDto> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String,MessageDto> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(this.consumerFactory());
        factory.setReplyTemplate(kafkaTemplate);
        factory.setCommonErrorHandler(errorHandler(kafkaTemplate));
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
                log.error("[CustomKafkaErrorHandler] Extracted message=[" + message + "], errorMessage=[" + e.getMessage() + "]");

                // 이후 처리할 메시지를 반환
                return message;  // message 값만 반환하여 sendTo 토픽으로 전송
            } catch (Exception ex) {
                log.error("[CustomKafkaErrorHandler] Failed to parse JSON payload, error: " + ex.getMessage());
                return m.getPayload();  // 파싱 실패 시 원본 payload 반환
            }
        };
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, MessageDto> kafkaTemplate) {

        // FixedBackOff 설정: 1초 간격으로 최대 3번 재시도
        FixedBackOff fixedBackOff = new FixedBackOff(5000L, 5L);

        // DefaultErrorHandler 생성
        return new DefaultErrorHandler(deadLetterPublishingRecoverer(kafkaTemplate), fixedBackOff);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, MessageDto> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> {
                    // 메시지를 전송할 토픽 지정
                    return new TopicPartition("dead-letter-topic", record.partition());
                });
    }

}

