package com.example.loanapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.loanapplication.client")
public class LoanApplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoanApplicationServiceApplication.class, args);
    }
}
