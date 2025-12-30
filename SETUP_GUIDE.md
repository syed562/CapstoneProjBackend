# Loan Management System - Backend Setup Guide

## Quick Start (5 Minutes)

### Prerequisites
- Java 17+ installed (`java -version`)
- PostgreSQL installed and running
- Maven 3.8+ installed (`mvn -v`)

### Step 1: Database Setup
```bash
# Create database
psql -U postgres -c "CREATE DATABASE loans_db;"

# Update database credentials in each service's application.properties
# Files to update:
# - auth-service/src/main/resources/application.properties
# - loan-application-service/src/main/resources/application.properties
# - loan-service/src/main/resources/application.properties
# - profile-service/src/main/resources/application.properties
# - report-service/src/main/resources/application.properties
```

### Step 2: Build All Services
```bash
cd backend-java
mvn clean install -DskipTests
```

### Step 3: Run Services (Each in Separate Terminal)

**Terminal 1 - Auth Service:**
```bash
cd backend-java/auth-service
mvn spring-boot:run
# Listens on: http://localhost:8083
```

**Terminal 2 - Loan Application Service:**
```bash
cd backend-java/loan-application-service
mvn spring-boot:run
# Listens on: http://localhost:8084
```

**Terminal 3 - Loan Service:**
```bash
cd backend-java/loan-service
mvn spring-boot:run
# Listens on: http://localhost:8085
```

**Terminal 4 - Profile Service:**
```bash
cd backend-java/profile-service
mvn spring-boot:run
# Listens on: http://localhost:8086
```

**Terminal 5 - Report Service:**
```bash
cd backend-java/report-service
mvn spring-boot:run
# Listens on: http://localhost:8087
```

---

## Complete Workflow Example

### 1. Register a New User (Customer)
**Endpoint:** `POST http://localhost:8083/api/auth/register`

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "SecurePass123",
    "role": "CUSTOMER"
  }'
```

**Response:**
```json
{
  "userId": "uuid-here",
  "username": "customer1",
  "email": "customer1@example.com",
  "role": "CUSTOMER",
  "message": "User registered successfully"
}
```

### 2. Login
**Endpoint:** `POST http://localhost:8083/api/auth/login`

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "SecurePass123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9...",
  "userId": "uuid-here",
  "username": "customer1",
  "role": "CUSTOMER",
  "message": "Login successful"
}
```

**Save the token:** Use this JWT token in all subsequent requests as:
```
Authorization: Bearer <token>
```

### 3. Update Customer Profile
**Endpoint:** `PUT http://localhost:8086/api/profiles/me?userId={userId}`

```bash
curl -X PUT "http://localhost:8086/api/profiles/me?userId=uuid-here" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "customer1@example.com",
    "phone": "9876543210",
    "addressLine1": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "postalCode": "10001",
    "country": "USA"
  }'
```

### 4. Apply for a Loan
**Endpoint:** `POST http://localhost:8084/api/loan-applications/apply`

```bash
curl -X POST http://localhost:8084/api/loan-applications/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "userId": "uuid-here",
    "amount": 500000,
    "termMonths": 60,
    "ratePercent": 9.5
  }'
```

**Response:**
```json
{
  "id": "app-uuid",
  "userId": "uuid-here",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 9.5,
  "status": "SUBMITTED",
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Save the application ID:** You'll need this for tracking the application.

### 5. Register Loan Officer
**Endpoint:** `POST http://localhost:8083/api/auth/register`

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "officer1",
    "email": "officer1@bank.com",
    "password": "OfficerPass123",
    "role": "LOAN_OFFICER"
  }'
```

### 6. Loan Officer Reviews Application
**Endpoint:** `PUT http://localhost:8084/api/loan-applications/{applicationId}/review`

```bash
curl -X PUT "http://localhost:8084/api/loan-applications/app-uuid/review" \
  -H "Authorization: Bearer <officer-token>" \
```

### 7. Loan Officer Approves Application
**Endpoint:** `PUT http://localhost:8084/api/loan-applications/{applicationId}/approve`

```bash
curl -X PUT "http://localhost:8084/api/loan-applications/app-uuid/approve" \
  -H "Authorization: Bearer <officer-token>" \
```

**Response:**
```json
{
  "id": "app-uuid",
  "userId": "uuid-here",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 9.5,
  "status": "APPROVED",
  "updatedAt": "2025-01-15T11:00:00Z"
}
```

### 8. Create Loan from Approved Application
**Endpoint:** `POST http://localhost:8085/api/loans/{applicationId}/approve`

```bash
curl -X POST "http://localhost:8085/api/loans/app-uuid/approve" \
  -H "Authorization: Bearer <officer-token>" \
```

**Response:**
```json
{
  "id": "loan-uuid",
  "userId": "uuid-here",
  "amount": 500000,
  "termMonths": 60,
  "ratePercent": 9.5,
  "status": "approved"
}
```

**Save the loan ID:** Required for EMI and repayment operations.

### 9. Generate EMI Schedule
**Endpoint:** `POST http://localhost:8085/api/loans/{loanId}/emi/generate`

```bash
curl -X POST "http://localhost:8085/api/loans/loan-uuid/emi/generate" \
  -H "Authorization: Bearer <token>" \
```

**Response:** Array of 60 EMI schedules
```json
[
  {
    "id": "emi-uuid",
    "loanId": "loan-uuid",
    "month": 1,
    "emiAmount": 9749.25,
    "principalAmount": 7549.25,
    "interestAmount": 2200.00,
    "outstandingBalance": 492450.75,
    "status": "SCHEDULED",
    "dueDate": "2025-02-28"
  },
  ...
]
```

### 10. Get EMI Schedule
**Endpoint:** `GET http://localhost:8085/api/loans/{loanId}/emi`

```bash
curl -X GET "http://localhost:8085/api/loans/loan-uuid/emi" \
  -H "Authorization: Bearer <token>" \
```

### 11. Record Payment
**Endpoint:** `POST http://localhost:8085/api/loans/{loanId}/repayments`

```bash
curl -X POST "http://localhost:8085/api/loans/loan-uuid/repayments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "emiScheduleId": "emi-uuid",
    "amount": 9749.25,
    "paymentMethod": "BANK_TRANSFER",
    "transactionId": "TXN20250115001"
  }'
```

### 12. Get Outstanding Balance
**Endpoint:** `GET http://localhost:8085/api/loans/{loanId}/repayments/outstanding-balance`

```bash
curl -X GET "http://localhost:8085/api/loans/loan-uuid/repayments/outstanding-balance" \
  -H "Authorization: Bearer <token>" \
```

### 13. Get Dashboard Statistics
**Endpoint:** `GET http://localhost:8087/api/reports/dashboard`

```bash
curl -X GET "http://localhost:8087/api/reports/dashboard" \
  -H "Authorization: Bearer <token>" \
```

**Response:**
```json
{
  "totalLoans": 10,
  "pendingLoans": 2,
  "approvedLoans": 7,
  "rejectedLoans": 1,
  "totalLoanAmount": 5000000,
  "approvedLoanAmount": 3500000,
  "approvalRate": 70.0
}
```

### 14. Get Loan Status Report
**Endpoint:** `GET http://localhost:8087/api/reports/loan-status`

```bash
curl -X GET "http://localhost:8087/api/reports/loan-status" \
  -H "Authorization: Bearer <token>" \
```

### 15. Get Customer Loan Summary
**Endpoint:** `GET http://localhost:8087/api/reports/customer-summary`

```bash
curl -X GET "http://localhost:8087/api/reports/customer-summary" \
  -H "Authorization: Bearer <token>" \
```

---

## Swagger Documentation

Access interactive API documentation for each service:

- **Auth Service:** http://localhost:8083/swagger-ui.html
- **Loan Application Service:** http://localhost:8084/swagger-ui.html
- **Loan Service:** http://localhost:8085/swagger-ui.html
- **Profile Service:** http://localhost:8086/swagger-ui.html
- **Report Service:** http://localhost:8087/swagger-ui.html

---

## Testing Checklist

### Authentication ✓
- [ ] User registration
- [ ] User login
- [ ] JWT token validation
- [ ] Role-based access control

### Loan Applications ✓
- [ ] Create application
- [ ] List user's applications
- [ ] Mark for review
- [ ] Approve application
- [ ] Reject application

### Loans & EMI ✓
- [ ] Create loan from application
- [ ] Generate EMI schedule
- [ ] View EMI details
- [ ] Calculate outstanding balance

### Repayments ✓
- [ ] Record payment
- [ ] View payment history
- [ ] Check outstanding balance
- [ ] Track completed payments

### Reports ✓
- [ ] Loan status distribution
- [ ] Customer summary
- [ ] Dashboard statistics

---

## Troubleshooting

### Port Already in Use
```bash
# Find process using port (e.g., 8083)
lsof -i :8083
# Kill process
kill -9 <PID>
```

### Database Connection Error
- Check PostgreSQL is running
- Verify database `loans_db` exists
- Check credentials in `application.properties`

### JWT Token Invalid
- Token may have expired (24-hour expiration)
- Verify token is passed as: `Authorization: Bearer <token>`
- Check service is running on correct port

### Maven Build Fails
```bash
# Clear cache and rebuild
mvn clean
mvn install -DskipTests
```

---

## Performance Tips

1. **Use Pagination** - For large result sets:
   ```
   GET /api/loans?page=0&size=10&sort=createdAt,desc
   ```

2. **Enable Connection Pooling** - Already configured with HikariCP (max 5 connections)

3. **Monitor Logs** - Check console output for slow queries

4. **Batch Operations** - Process multiple payments in single request when possible

---

## Security Best Practices

1. **Keep JWT Secret Secure** - Change `app.jwt.secret` in production
2. **Use HTTPS** - Required for production deployments
3. **Database Backups** - Regular backups of `loans_db`
4. **Access Control** - Only loan officers can approve applications
5. **Audit Logs** - Track all state changes (implement later)

---

**Last Updated:** January 2025  
**Version:** 1.0.0
