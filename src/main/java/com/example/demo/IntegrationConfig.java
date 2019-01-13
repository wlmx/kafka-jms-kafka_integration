package com.example.demo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageChannel;

import javax.jms.ConnectionFactory;

@Configuration
@EnableConfigurationProperties({JmsConfigProperties.class, KafkaConfigProperties.class})
@EnableIntegration
@IntegrationComponentScan
public class IntegrationConfig {

    private JmsConfigProperties jmsConfigProperties;

    private KafkaConfigProperties kafkaConfigProperties;

    @Autowired
    public IntegrationConfig(JmsConfigProperties jmsConfigProperties, KafkaConfigProperties kafkaConfigProperties) {
        this.jmsConfigProperties = jmsConfigProperties;
        this.kafkaConfigProperties = kafkaConfigProperties;
    }


    @Bean
    public MessageChannel jmsRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel jmsResponseChannel() {
        return new DirectChannel();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new CachingConnectionFactory(
                new ActiveMQConnectionFactory(jmsConfigProperties.getBrokerUrl()));
    }

    @Bean
    public IntegrationFlow jmsFlow() {
        return IntegrationFlows
                .from(jmsRequestChannel())
                .handle(Jms.outboundGateway(connectionFactory())
                                .receiveTimeout(jmsConfigProperties.getTimeout())
                                .requestDestination(jmsConfigProperties.getRequestQueue())
                                .replyDestination(jmsConfigProperties.getResponseQueue())
                                .correlationKey("JMSCorrelationID"),
                        endpointSpec -> endpointSpec.requiresReply(true))
                .channel(jmsResponseChannel()).get();
    }


    @Bean
    public MessageChannel kafkaRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel kafkaResponseChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow toKafkaFlow(KafkaTemplate<?, ?> kafkaTemplate) {
        return IntegrationFlows
                .from(kafkaResponseChannel())
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate)
                        .topic(kafkaConfigProperties.getResponseTopic()))
                .get();
    }

    @Bean
    public IntegrationFlow fromKafkaFlow(ConsumerFactory<?, ?> consumerFactory) {
        return IntegrationFlows
                .from(Kafka.messageDrivenChannelAdapter(consumerFactory, this.kafkaConfigProperties.getRequestTopic()))
                .channel(kafkaRequestChannel())
                .get();
    }

    @Bean
    public MessageProcessor messageProcessor(JmsGateway jmsGateway, KafkaGateway kafkaGateway) {
        return new MessageProcessor(jmsGateway, kafkaGateway);
    }

    @Bean
    public IntegrationFlow handlerFlow() {
        return IntegrationFlows.from(kafkaRequestChannel())
                .handle("messageProcessor", "process")
                .get();
    }

}