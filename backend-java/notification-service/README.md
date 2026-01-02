# Notification Service

Spring Boot microservice for sending email notifications for loan events.

## Features
- **Loan Application Notifications** - Email when application is submitted, approved, or rejected
- **EMI Due Reminders** - Send reminders before EMI due date
- **EMI Overdue Alerts** - Send urgent notifications for overdue EMIs
- **Loan Closure Notifications** - Confirm when loan is completely paid

## Architecture
- **Message Broker**: RabbitMQ (asynchronous event processing)
- **Email Service**: JavaMailSender with SMTP (Gmail)
- **Event-Driven**: Decoupled from other services via message queues

## RabbitMQ Queues

### Loan Application Events
- **Exchange**: `loan-application-exchange`
- **Queue**: `loan-application-queue`
- **Events**: CREATED, APPROVED, REJECTED, UNDER_REVIEW

### EMI Events
- **Exchange**: `emi-exchange`
- **Queues**: 
  - `emi-due-queue` - For EMI due reminders
  - `emi-overdue-queue` - For overdue EMI alerts
  - `loan-closure-queue` - For loan closure notifications

## Configuration

### Email Setup (Gmail)
1. Enable 2-Factor Authentication on Gmail
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Set environment variables:
   ```bash
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```

### RabbitMQ Setup
Ensure RabbitMQ is running:
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

## How to Integrate

### 1. In Loan Application Service
```java
@Service
public class LoanApplicationService {
    private final NotificationEventPublisher publisher;
    
    public void createApplication(LoanApplicationRequest request) {
        // ... create application ...
        
        LoanApplicationEvent event = new LoanApplicationEvent();
        event.setEventType("CREATED");
        event.setApplicationId(application.getId());
        event.setUserId(userId);
        event.setUserEmail(userEmail);
        event.setUserName(userName);
        event.setLoanAmount(request.getLoanAmount());
        
        publisher.publishLoanApplicationEvent(event);
    }
}
```

### 2. In Loan Service (for EMI events)
```java
@Service
public class EMIService {
    private final NotificationEventPublisher publisher;
    
    public void checkAndNotifyEMIDue() {
        // ... check for due EMIs ...
        
        EMIEvent event = new EMIEvent();
        event.setEventType("EMI_DUE");
        event.setLoanId(loan.getId());
        event.setUserEmail(user.getEmail());
        // ... populate other fields ...
        
        publisher.publishEMIDueEvent(event);
    }
}
```

## Running the Service

```bash
cd notification-service
mvn spring-boot:run
```

Port: 8086

## Email Templates

### Loan Application Approved
```
Dear [User Name],

Your loan application has been APPROVED.

Application ID: [ID]
Loan Amount: ₹[Amount]

Congratulations! Your loan has been approved. The amount will be 
disbursed to your account within 1-2 business days.

Thank you,
Loan Management System Team
```

### EMI Due Reminder
```
Dear [User Name],

This is a reminder that your EMI payment is due.

Loan ID: [ID]
Month: [Number]
EMI Amount: ₹[Amount]
Due Date: [Date]

Please make the payment before the due date to avoid penalties.
```

### EMI Overdue Alert
```
Dear [User Name],

⚠️ IMPORTANT: Your EMI payment is OVERDUE

Loan ID: [ID]
Overdue Amount: ₹[Amount]
Outstanding Balance: ₹[Balance]

Please make the payment immediately to avoid penalties and legal action.
Contact our support team for assistance.
```

## Troubleshooting

### Emails Not Sending
1. Check MAIL_USERNAME and MAIL_PASSWORD environment variables
2. Verify Gmail App Password is set correctly
3. Enable "Less secure app access" in Gmail (if not using App Password)

### RabbitMQ Connection Issues
1. Verify RabbitMQ is running on localhost:5672
2. Check credentials in application.properties
3. Verify queue/exchange configuration

## Dependencies
- Spring Boot 3.4.1
- Spring AMQP (RabbitMQ)
- Spring Mail
- Spring Cloud Eureka
- Lombok
- PostgreSQL

---

Port: 8086 | Database: PostgreSQL (loans_db)
