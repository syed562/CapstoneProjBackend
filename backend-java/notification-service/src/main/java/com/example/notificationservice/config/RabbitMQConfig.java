package com.example.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
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
    public Binding loanApplicationBinding(Queue loanApplicationQueue, TopicExchange loanApplicationExchange) {
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
    public Binding emiDueBinding(Queue emiDueQueue, TopicExchange emiExchange) {
        return BindingBuilder.bind(emiDueQueue)
                .to(emiExchange)
                .with("emi.due");
    }
    
    @Bean
    public Binding emiOverdueBinding(Queue emiOverdueQueue, TopicExchange emiExchange) {
        return BindingBuilder.bind(emiOverdueQueue)
                .to(emiExchange)
                .with("emi.overdue");
    }
    
    @Bean
    public Binding loanClosureBinding(Queue loanClosureQueue, TopicExchange emiExchange) {
        return BindingBuilder.bind(loanClosureQueue)
                .to(emiExchange)
                .with("loan.closed");
    }
}
