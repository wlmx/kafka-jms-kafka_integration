package com.example.demo;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "jmsRequestChannel", defaultReplyChannel = "jmsResponseChannel")
public interface JmsGateway {

    @Gateway
    String sendAndReceive(String message);
}
