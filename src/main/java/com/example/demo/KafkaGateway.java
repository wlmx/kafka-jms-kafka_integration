package com.example.demo;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "kafkaResponseChannel", defaultReplyChannel = "kafkaRequestChannel")
public interface KafkaGateway {

    @Gateway
    String receive();

    @Gateway
    void send(String payload);
}
