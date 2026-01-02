package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType; // CREATED, APPROVED, REJECTED, UNDER_REVIEW
    private String applicationId;
    private String userId;
    private String userEmail;
    private String userName;
    private Double loanAmount;
    private String remarks;
    private long timestamp;
}
