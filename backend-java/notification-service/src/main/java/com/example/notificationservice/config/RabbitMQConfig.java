package com.example.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Loan Application Queues & Exchanges
    public static final String LOAN_APPLICATION_EXCHANGE = "loan-application-exchange";
    public static final String LOAN_APPLICATION_QUEUE = "loan-application-queue";
    public static final String LOAN_APPLICATION_ROUTING_KEY = "loan.application.*";
    
    // EMI Queues & Exchanges
    public static final String EMI_EXCHANGE = "emi-exchange";
    public static final String EMI_DUE_QUEUE = "emi-due-queue";
    public static final String EMI_OVERDUE_QUEUE = "emi-overdue-queue";
    public static final String LOAN_CLOSURE_QUEUE = "loan-closure-queue";
    public static final String EMI_ROUTING_KEY = "emi.*";
    
    // Jackson Message Converter for JSON serialization
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Use JSON converter for all listeners to avoid Java serialization
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
    
    // Loan Application Configuration
    @Bean
    public TopicExchange loanApplicationExchange() {
        return new TopicExchange(LOAN_APPLICATION_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue loanApplicationQueue() {
        return new Queue(LOAN_APPLICATION_QUEUE, true);
    }
    
    @Bean
        public Binding loanApplicationBinding(
            @Qualifier("loanApplicationQueue") Queue loanApplicationQueue,
            @Qualifier("loanApplicationExchange") TopicExchange loanApplicationExchange) {
        return BindingBuilder.bind(loanApplicationQueue)
                .to(loanApplicationExchange)
                .with(LOAN_APPLICATION_ROUTING_KEY);
    }
    
    // EMI Configuration
    @Bean
    public TopicExchange emiExchange() {
        return new TopicExchange(EMI_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue emiDueQueue() {
        return new Queue(EMI_DUE_QUEUE, true);
    }
    
    @Bean
    public Queue emiOverdueQueue() {
        return new Queue(EMI_OVERDUE_QUEUE, true);
    }
    
    @Bean
    public Queue loanClosureQueue() {
        return new Queue(LOAN_CLOSURE_QUEUE, true);
    }
    
    @Bean
        public Binding emiDueBinding(
            @Qualifier("emiDueQueue") Queue emiDueQueue,
            @Qualifier("emiExchange") TopicExchange emiExchange) {
        return BindingBuilder.bind(emiDueQueue)
                .to(emiExchange)
                .with("emi.due");
    }
    
    @Bean
        public Binding emiOverdueBinding(
            @Qualifier("emiOverdueQueue") Queue emiOverdueQueue,
            @Qualifier("emiExchange") TopicExchange emiExchange) {
        return BindingBuilder.bind(emiOverdueQueue)
                .to(emiExchange)
                .with("emi.overdue");
    }
    
    @Bean
        public Binding loanClosureBinding(
            @Qualifier("loanClosureQueue") Queue loanClosureQueue,
            @Qualifier("emiExchange") TopicExchange emiExchange) {
        return BindingBuilder.bind(loanClosureQueue)
                .to(emiExchange)
                .with("loan.closed");
    }
}
