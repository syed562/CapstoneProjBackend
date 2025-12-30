# Quick Start Guide - Running the Loan Management System

This guide will help you start all 5 microservices and test the complete loan management workflow.

## Prerequisites

- Java 17 installed
- PostgreSQL database running on localhost:5432
- Maven 3.8+ (optional, since JARs are pre-built)
- A terminal or PowerShell window

## Database Setup

Before running the services, ensure PostgreSQL is running:

```bash
# PostgreSQL should be accessible on localhost:5432
# Default credentials: username=postgres, password=postgres
```

Each service has `spring.jpa.hibernate.ddl-auto=update` configured, which will automatically create tables on first run.

## Starting the Services

Open 5 terminal windows and start each service:

### Terminal 1 - Auth Service (Port 8083)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java\auth-service
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```
**Expected Output:** "Started AuthServiceApplication in X seconds"  
**API Docs:** http://localhost:8083/swagger-ui.html

### Terminal 2 - Loan Application Service (Port 8084)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java\loan-application-service
java -jar target/loan-application-service-0.0.1-SNAPSHOT.jar
```
**Expected Output:** "Started LoanApplicationServiceApplication in X seconds"  
**API Docs:** http://localhost:8084/swagger-ui.html

### Terminal 3 - Loan Service (Port 8085)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java\loan-service
java -jar target/loan-service-0.0.1-SNAPSHOT.jar
```
**Expected Output:** "Started LoanServiceApplication in X seconds"  
**API Docs:** http://localhost:8085/swagger-ui.html

### Terminal 4 - Profile Service (Port 8086)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java\profile-service
java -jar target/profile-service-0.0.1-SNAPSHOT.jar
```
**Expected Output:** "Started ProfileServiceApplication in X seconds"  
**API Docs:** http://localhost:8086/swagger-ui.html

### Terminal 5 - Report Service (Port 8087)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java\report-service
java -jar target/report-service-0.0.1-SNAPSHOT.jar
```
**Expected Output:** "Started ReportServiceApplication in X seconds"  
**API Docs:** http://localhost:8087/swagger-ui.html

---

## Testing Complete Workflow

### Step 1: Register a Customer

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "Password@123",
    "role": "CUSTOMER"
  }'
```

**Response:** (Save the userId from response)
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "customer1",
  "email": "customer1@example.com",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "message": "User registered successfully"
}
```

### Step 2: Login & Get JWT Token

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "Password@123"
  }'
```

**Response:** (Save the token)
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "customer1",
  "role": "CUSTOMER",
  "expiresIn": 86400000
}
```

### Step 3: Update Customer Profile

```bash
curl -X PUT http://localhost:8086/api/profile/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "customer1@example.com",
    "phone": "9876543210",
    "address": "123 Main St, City, State 12345"
  }'
```

### Step 4: Apply for a Loan

```bash
curl -X POST http://localhost:8084/api/loan-applications/apply \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500000,
    "termMonths": 60,
    "ratePercent": 8.5
  }'
```

**Response:** (Save the applicationId)
```json
{
  "applicationId": "app-123",
  "userId": "user-123",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 8.5,
  "status": "SUBMITTED",
  "createdAt": "2025-12-30T16:30:00Z"
}
```

### Step 5: Register Loan Officer & Approve Application

First, register a Loan Officer:
```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "officer1",
    "email": "officer1@example.com",
    "password": "Password@123",
    "role": "LOAN_OFFICER"
  }'
```

Login as Loan Officer and get token, then approve the application:
```bash
curl -X POST "http://localhost:8084/api/loan-applications/{applicationId}/approve" \
  -H "Authorization: Bearer LOAN_OFFICER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "remarks": "Approved based on credit score and income verification"
  }'
```

### Step 6: Generate EMI Schedule

Once application is approved, generate EMI schedule:
```bash
curl -X POST http://localhost:8085/api/loans/loan-from-application/{applicationId} \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

Then generate EMI schedule:
```bash
curl -X POST "http://localhost:8085/api/emi/generate/{loanId}" \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

### Step 7: Record a Repayment

```bash
curl -X POST http://localhost:8085/api/repayments/record \
  -H "Authorization: Bearer CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "loanId": "loan-123",
    "emiScheduleId": "emi-1",
    "amountPaid": 11920.67,
    "paymentDate": "2025-12-30",
    "paymentMethod": "BANK_TRANSFER",
    "transactionId": "TXN123456"
  }'
```

### Step 8: View Reports & Dashboard

Get loan status distribution:
```bash
curl -X GET http://localhost:8087/api/reports/loan-status \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

Get dashboard statistics:
```bash
curl -X GET http://localhost:8087/api/reports/dashboard \
  -H "Authorization: Bearer CUSTOMER_TOKEN"
```

Get customer loan summary:
```bash
curl -X GET "http://localhost:8087/api/reports/customer-summary?userId=user-123" \
  -H "Authorization: Bearer LOAN_OFFICER_TOKEN"
```

---

## Troubleshooting

### Service fails to start
- Check PostgreSQL is running: `psql -U postgres -c "SELECT version();"`
- Check port is not in use: `netstat -ano | findstr :8083` (for auth-service)
- Check application.properties configuration

### JWT Token Invalid
- Ensure token is passed with "Bearer " prefix
- Check token hasn't expired (default: 24 hours)
- Verify token is from correct service (auth-service)

### Database Connection Error
- Verify PostgreSQL credentials in application.properties
- Check database "capstone_db" exists (auto-created if configured)
- Ensure PostgreSQL JDBC driver is available

### Compile Errors (if building from source)
```bash
cd c:\Users\syeds\Downloads\Capstone_Backend\backend-java
mvn clean install -DskipTests
```

---

## API Documentation

Each service has Swagger UI documentation available:
- **Auth Service:** http://localhost:8083/swagger-ui.html
- **Loan Application Service:** http://localhost:8084/swagger-ui.html
- **Loan Service:** http://localhost:8085/swagger-ui.html
- **Profile Service:** http://localhost:8086/swagger-ui.html
- **Report Service:** http://localhost:8087/swagger-ui.html

---

## Key Features Implemented

✅ JWT Authentication with Role-Based Access Control (RBAC)  
✅ Complete Loan Application Workflow (SUBMITTED → APPROVED → CLOSED)  
✅ EMI Calculation with 60-month schedule  
✅ Repayment Tracking with transaction history  
✅ Customer Profile Management with KYC status  
✅ Analytics & Reports with Stream-based aggregations  
✅ Global Exception Handling with consistent error responses  
✅ OpenAPI/Swagger documentation for all endpoints  
✅ Service-to-service communication via Feign clients  

---

## Next Steps

1. **Run all 5 services** following the terminal instructions above
2. **Test the complete workflow** using the curl examples provided
3. **Explore Swagger UI** at each service's documentation endpoint
4. **Monitor PostgreSQL** to see tables being created automatically
5. **Test with Angular frontend** (separate project) for complete system

---

**For detailed implementation information, see IMPLEMENTATION_SUMMARY.md**
