package com.example.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EMIEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String eventType; // EMI_DUE, EMI_OVERDUE, LOAN_CLOSED
    private String loanId;
    private String userId;
    private String userEmail;
    private String userName;
    private Double emiAmount;
    private LocalDate dueDate;
    private Integer monthNumber;
    private Double outstandingBalance;
    private long timestamp;
}
