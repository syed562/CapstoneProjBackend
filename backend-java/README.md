# Loan Management System - Backend (Java Spring Boot)

## Project Overview
A secure, scalable, and modern microservices-based Loan Management System built with Java Spring Boot. This backend provides REST APIs for managing the complete loan lifecycle including applications, approvals, EMI calculations, repayments, and reporting.

## Tech Stack
- **Java 17+**
- **Spring Boot 3.3.5**
- **Spring Cloud (Feign, Service-to-Service Communication)**
- **Spring Security + JWT**
- **Spring Data JPA + Hibernate**
- **PostgreSQL**
- **Maven**
- **Springdoc OpenAPI (Swagger)**

## Microservices Architecture

### 1. **Auth Service** (Port: 8083)
User authentication and authorization service with JWT tokens.

**Key Features:**
- User registration and login
- JWT token generation
- Role-based access control (ADMIN, LOAN_OFFICER, CUSTOMER)
- Password hashing with BCrypt

**Endpoints:**
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/register` - Register new user
- `GET /api/auth/users/{userId}` - Get user details

**Database Tables:**
- `users` - Stores user credentials and roles

---

### 2. **Loan Application Service** (Port: 8084)
Manages loan applications with status tracking.

**Key Features:**
- Submit loan applications
- Track application status (SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED → CLOSED)
- Loan officer approval/rejection workflow
- Application history and remarks

**Endpoints:**
- `POST /api/loan-applications/apply` - Submit new application
- `GET /api/loan-applications` - List all applications (admin)
- `GET /api/loan-applications/my?userId=X` - User's applications
- `GET /api/loan-applications/{id}` - Get application details
- `PUT /api/loan-applications/{id}/review` - Mark for review
- `PUT /api/loan-applications/{id}/approve` - Approve application
- `PUT /api/loan-applications/{id}/reject` - Reject application

**Database Tables:**
- `loan_applications` - Application records with status tracking

---

### 3. **Loan Service** (Port: 8085)
Manages approved loans and EMI/repayment tracking.

**Key Features:**
- Create loans from approved applications
- EMI schedule generation
- Repayment tracking
- Outstanding balance calculation
- Pagination support

**Endpoints:**
- `GET /api/loans` - List all loans
- `GET /api/loans/{id}` - Get loan details
- `POST /api/loans` - Create loan
- `PATCH /api/loans/{id}/status` - Update loan status
- `POST /api/loans/{applicationId}/approve` - Create loan from application
- `GET /api/loans/{loanId}/emi` - Get EMI schedule
- `POST /api/loans/{loanId}/emi/generate` - Generate EMI schedule
- `POST /api/loans/{loanId}/repayments` - Record payment
- `GET /api/loans/{loanId}/repayments` - Get repayment history
- `GET /api/loans/{loanId}/repayments/outstanding-balance` - Outstanding amount

**Database Tables:**
- `loans` - Active and closed loans
- `emi_schedules` - Monthly EMI schedule
- `repayments` - Payment transaction records

**EMI Calculation Formula:**
```
EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]

Where:
P = Principal (loan amount)
r = Monthly interest rate (annual rate / 12 / 100)
n = Number of months
```

---

### 4. **Profile Service** (Port: 8086)
Manages customer profiles and KYC status.

**Key Features:**
- Customer profile management
- KYC (Know Your Customer) verification
- Profile updates
- Address and identity information

**Endpoints:**
- `GET /api/profiles/me?userId=X` - Get own profile
- `PUT /api/profiles/me?userId=X` - Update own profile
- `GET /api/profiles/{userId}` - Get user profile (admin)
- `PUT /api/profiles/{userId}/kyc` - Update KYC status

**Database Tables:**
- `profiles` - Customer profile information

---

### 5. **Report Service** (Port: 8087)
Analytics and reporting service for dashboards and business intelligence.

**Key Features:**
- Loan status distribution reports
- Customer-wise loan summaries
- Dashboard statistics
- Uses Java 8+ Streams for calculations

**Endpoints:**
- `GET /api/reports/loan-status` - Loans by status
- `GET /api/reports/customer-summary` - Per-customer analysis
- `GET /api/reports/dashboard` - Dashboard metrics

**Calculated Metrics:**
- Total and active loan counts
- Approval rates
- Outstanding amounts per customer
- Revenue aggregations

---

## Database Schema

### Core Tables

**users**
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN, LOAN_OFFICER, CUSTOMER
    status VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, LOCKED
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**loan_applications**
```sql
CREATE TABLE loan_applications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    term_months INT NOT NULL,
    rate_percent DECIMAL(5, 2),
    status VARCHAR(20) NOT NULL, -- SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CLOSED
    remarks TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**loans**
```sql
CREATE TABLE loans (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    term_months INT NOT NULL,
    rate_percent DECIMAL(5, 2),
    status VARCHAR(20) NOT NULL, -- pending, approved, closed
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**emi_schedules**
```sql
CREATE TABLE emi_schedules (
    id VARCHAR(36) PRIMARY KEY,
    loan_id VARCHAR(36) NOT NULL,
    month INT NOT NULL,
    emi_amount DECIMAL(15, 2) NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_amount DECIMAL(15, 2) NOT NULL,
    outstanding_balance DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- SCHEDULED, PAID, OVERDUE
    paid_date DATE,
    due_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loans(id)
);
```

**repayments**
```sql
CREATE TABLE repayments (
    id VARCHAR(36) PRIMARY KEY,
    loan_id VARCHAR(36) NOT NULL,
    emi_schedule_id VARCHAR(36) NOT NULL,
    amount_paid DECIMAL(15, 2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    status VARCHAR(20) NOT NULL, -- COMPLETED, FAILED, PENDING
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES loans(id),
    FOREIGN KEY (emi_schedule_id) REFERENCES emi_schedules(id)
);
```

**profiles**
```sql
CREATE TABLE profiles (
    user_id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    kyc_status VARCHAR(20), -- PENDING, APPROVED, REJECTED
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Setup Instructions

### Prerequisites
- Java 17 or higher
- PostgreSQL 12+
- Maven 3.8+

### Database Setup
1. Create PostgreSQL database:
```bash
createdb loans_db
```

2. Update credentials in each service's `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/loans_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

3. Tables will be created automatically via Hibernate `ddl-auto=update`

### Running the Services

1. **Clone the repository:**
```bash
cd backend-java
```

2. **Build all services:**
```bash
mvn clean install
```

3. **Start each service (in separate terminals):**

**Auth Service:**
```bash
cd auth-service
mvn spring-boot:run
```

**Loan Application Service:**
```bash
cd loan-application-service
mvn spring-boot:run
```

**Loan Service:**
```bash
cd loan-service
mvn spring-boot:run
```

**Profile Service:**
```bash
cd profile-service
mvn spring-boot:run
```

**Report Service:**
```bash
cd report-service
mvn spring-boot:run
```

### Accessing Swagger UI
Each service has Swagger documentation available at:
- Auth Service: http://localhost:8083/swagger-ui.html
- Loan Application Service: http://localhost:8084/swagger-ui.html
- Loan Service: http://localhost:8085/swagger-ui.html
- Profile Service: http://localhost:8086/swagger-ui.html
- Report Service: http://localhost:8087/swagger-ui.html

---

## API Usage Examples

### 1. Register a New User
```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

### 3. Apply for Loan
```bash
curl -X POST http://localhost:8084/api/loan-applications/apply \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -d '{
    "userId": "user-123",
    "amount": 100000,
    "termMonths": 24,
    "ratePercent": 8.5
  }'
```

### 4. Get Dashboard Statistics
```bash
curl -X GET http://localhost:8087/api/reports/dashboard \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway / Frontend                   │
└──────────────┬────────────────────────────────────┬──────────┘
               │                                    │
       ┌───────▼─────────┐              ┌──────────▼────────┐
       │  Auth Service   │              │ Loan App Service  │
       │   (Port 8083)   │              │  (Port 8084)      │
       └─────────────────┘              └───────────────────┘
               │
       ┌───────▼─────────┐              ┌──────────▼────────┐
       │  Loan Service   │              │ Profile Service   │
       │   (Port 8085)   │              │  (Port 8086)      │
       │  - EMI/Repay    │              │  - KYC/Profile    │
       └─────────────────┘              └───────────────────┘
               │
       ┌───────▼─────────┐
       │ Report Service  │
       │  (Port 8087)    │
       │  - Analytics    │
       └─────────────────┘
               │
       ┌───────▼──────────┐
       │  PostgreSQL DB   │
       │   (loans_db)     │
       └──────────────────┘
```

---

## Security

### Authentication & Authorization
- JWT tokens with HS512 algorithm
- Token expiration: 24 hours (configurable)
- Role-based access control:
  - **ADMIN**: Full system access
  - **LOAN_OFFICER**: Review and approve applications
  - **CUSTOMER**: Apply for loans, view own data

### Password Security
- BCrypt password hashing (strength: 12)
- No plaintext passwords stored

### CORS Configuration
- Allowed origins: `http://localhost:4200`, `http://localhost:3000`
- Allowed methods: GET, POST, PUT, DELETE, PATCH, OPTIONS

---

## Error Handling

All services implement centralized exception handling with consistent error response format:

```json
{
  "status": 400,
  "message": "Validation failed",
  "path": "/api/loan-applications/apply",
  "timestamp": "2025-01-01T10:30:00Z",
  "errors": {
    "amount": "must be positive",
    "userId": "is required"
  }
}
```

---

## Testing

### Unit Tests
Run tests with:
```bash
mvn test
```

### API Testing with Postman
Import the Postman collection (to be provided) and test all endpoints.

---

## Future Enhancements

1. **Notification Service** - Email/SMS alerts for loan status
2. **Document Management** - Upload and verify loan documents
3. **Advanced Analytics** - ML-based credit scoring
4. **Mobile App** - Native Android/iOS applications
5. **Cloud Deployment** - Docker, Kubernetes setup
6. **API Gateway** - Centralized routing with rate limiting

---

## Contact & Support

For questions or issues, please refer to the development team documentation.

---

**Last Updated:** January 2025  
**Version:** 1.0.0
