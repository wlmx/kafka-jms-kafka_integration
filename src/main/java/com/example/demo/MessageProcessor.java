package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;

public class MessageProcessor {

    private JmsGateway jmsGateway;

    private KafkaGateway kafkaGateway;

    @Autowired
    public MessageProcessor(JmsGateway jmsGateway, KafkaGateway kafkaGateway) {
        this.jmsGateway = jmsGateway;
        this.kafkaGateway = kafkaGateway;
    }

    public void process(String message) {
        System.out.println("kafka in: " + message);
        String response = jmsGateway.sendAndReceive(message);
        System.out.println("jms out: " + response);
        kafkaGateway.send(response);
        System.out.println("kafka out: " + response);
    }
}
