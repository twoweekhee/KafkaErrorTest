package com.test.kafkaerrortest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageDto {

    String message;
    int port;

    @JsonCreator
    public MessageDto(@JsonProperty("message") String message, @JsonProperty("port") int port) {
        this.message = message;
        this.port = port;
    }
}
