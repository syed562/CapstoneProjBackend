package com.example.loanservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.loanservice.service.LoanService;

@SpringBootTest
class LoanServiceContextTest {

    @Autowired
    private LoanService loanService;

    @Test
    void contextLoads() {
        assertNotNull(loanService, "LoanService should be autowired");
    }
}
