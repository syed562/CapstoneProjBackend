package com.example.loanapplication.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQProducerConfig {
    
    public static final String LOAN_APPLICATION_EXCHANGE = "loan-application-exchange";
    public static final String EMI_EXCHANGE = "emi-exchange";
    
    @Bean
    public TopicExchange loanApplicationExchange() {
        return new TopicExchange(LOAN_APPLICATION_EXCHANGE, true, false);
    }
    
    @Bean
    public TopicExchange emiExchange() {
        return new TopicExchange(EMI_EXCHANGE, true, false);
    }

    // Force all outbound messages to use JSON instead of Java serialization
    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter producerMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerMessageConverter);
        return rabbitTemplate;
    }
}
