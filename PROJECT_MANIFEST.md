# Project File Structure & Manifest

Complete directory structure and description of all files in the Capstone Loan Management System.

---

## Root Directory

```
Capstone_Backend/
├── backend-java/                          # Main Java microservices project
├── frontend/                              # Placeholder for Angular frontend
├── README.md                              # Project overview
├── QUICK_START.md                         # How to run and test the system
├── DELIVERY_SUMMARY.md                    # What was delivered
├── CONFIGURATION_GUIDE.md                 # Configuration reference
└── PROJECT_MANIFEST.md                    # This file
```

---

## Backend Java Project Structure

```
backend-java/
├── pom.xml                                # Parent Maven POM
│
├── auth-service/
│   ├── pom.xml
│   ├── README.md
│   ├── target/
│   │   └── auth-service-0.0.1-SNAPSHOT.jar
│   └── src/main/
│       ├── java/com/example/authservice/
│       │   ├── AuthServiceApplication.java
│       │   ├── config/
│       │   │   └── SecurityConfig.java              # Spring Security + CORS config
│       │   ├── controller/
│       │   │   └── AuthController.java              # REST endpoints
│       │   ├── dto/
│       │   │   ├── LoginRequest.java
│       │   │   ├── LoginResponse.java
│       │   │   ├── RegisterRequest.java
│       │   │   └── RegisterResponse.java
│       │   ├── entity/
│       │   │   └── User.java                        # User entity with roles
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java      # Centralized error handling
│       │   ├── repository/
│       │   │   └── UserRepository.java              # JPA repository
│       │   ├── security/
│       │   │   ├── JwtAuthenticationFilter.java     # JWT extraction filter
│       │   │   └── JwtTokenProvider.java            # Token generation/validation
│       │   └── service/
│       │       └── AuthService.java                 # Business logic
│       └── resources/
│           └── application.properties               # Configuration
│
├── loan-application-service/
│   ├── pom.xml
│   ├── README.md
│   ├── target/
│   │   └── loan-application-service-0.0.1-SNAPSHOT.jar
│   └── src/main/
│       ├── java/com/example/loanapplication/
│       │   ├── LoanApplicationServiceApplication.java
│       │   ├── controller/
│       │   │   └── LoanApplicationController.java   # 7 REST endpoints
│       │   ├── dto/
│       │   │   ├── ApplyRequest.java                # Loan application request
│       │   │   └── ApprovalRequest.java             # Approval/rejection request
│       │   ├── entity/
│       │   │   └── LoanApplication.java             # Application with status
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── repository/
│       │   │   └── LoanApplicationRepository.java   # Custom queries
│       │   ├── service/
│       │   │   └── LoanApplicationService.java      # Workflow logic
│       │   └── model/
│       │       └── ApplicationStatus.java           # SUBMITTED, UNDER_REVIEW, etc.
│       └── resources/
│           └── application.properties
│
├── loan-service/
│   ├── pom.xml
│   ├── README.md
│   ├── target/
│   │   └── loan-service-0.0.1-SNAPSHOT.jar
│   └── src/main/
│       ├── java/com/example/loanservice/
│       │   ├── LoanServiceApplication.java
│       │   ├── controller/
│       │   │   ├── LoanController.java              # Loan endpoints
│       │   │   ├── EMIController.java               # EMI schedule endpoints
│       │   │   └── RepaymentController.java         # Repayment endpoints
│       │   ├── domain/
│       │   │   ├── Loan.java                        # Loan entity
│       │   │   ├── EMISchedule.java                 # Monthly EMI details
│       │   │   └── Repayment.java                   # Payment transaction
│       │   ├── dto/
│       │   │   ├── LoanDTO.java                     # Loan data transfer
│       │   │   └── PaymentRequest.java              # Payment recording
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── service/
│       │   │   ├── LoanService.java                 # Loan management
│       │   │   ├── EMIService.java                  # EMI calculation
│       │   │   ├── EMICalculator.java               # Financial formula
│       │   │   └── RepaymentService.java            # Payment tracking
│       │   ├── repository/
│       │   │   ├── LoanRepository.java
│       │   │   ├── EMIScheduleRepository.java
│       │   │   └── RepaymentRepository.java
│       │   └── client/
│       │       └── dto/
│       │           └── LoanDTO.java                 # For report service
│       └── resources/
│           └── application.properties
│
├── profile-service/
│   ├── pom.xml
│   ├── README.md
│   ├── target/
│   │   └── profile-service-0.0.1-SNAPSHOT.jar
│   └── src/main/
│       ├── java/com/example/profileservice/
│       │   ├── ProfileServiceApplication.java
│       │   ├── controller/
│       │   │   └── ProfileController.java           # 4 REST endpoints
│       │   ├── domain/
│       │   │   └── Profile.java                     # Customer profile entity
│       │   ├── dto/
│       │   │   ├── UpdateProfileRequest.java
│       │   │   └── UpdateKycStatusRequest.java
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── service/
│       │   │   └── ProfileService.java              # Profile management
│       │   └── repository/
│       │       └── ProfileRepository.java
│       └── resources/
│           └── application.properties
│
├── report-service/
│   ├── pom.xml
│   ├── README.md
│   ├── target/
│   │   └── report-service-0.0.1-SNAPSHOT.jar
│   └── src/main/
│       ├── java/com/example/reportservice/
│       │   ├── ReportServiceApplication.java
│       │   ├── client/
│       │   │   ├── LoanClient.java                  # Feign client for loan service
│       │   │   └── dto/
│       │   │       └── LoanDTO.java
│       │   ├── controller/
│       │   │   └── ReportController.java            # 3 reporting endpoints
│       │   ├── dto/
│       │   │   ├── LoanStatusReportDTO.java
│       │   │   └── CustomerLoanSummaryDTO.java
│       │   ├── exception/
│       │   │   └── GlobalExceptionHandler.java
│       │   └── service/
│       │       └── ReportService.java               # Analytics using Streams
│       └── resources/
│           └── application.properties
│
└── target/                                # Build artifacts from parent POM
```

---

## Documentation Files

### At Root Level

| File | Purpose |
|------|---------|
| `README.md` | Project overview, architecture, and quick introduction |
| `QUICK_START.md` | Step-by-step guide to run services and test workflow |
| `DELIVERY_SUMMARY.md` | Complete list of deliverables and implementation status |
| `CONFIGURATION_GUIDE.md` | All configuration options and environment setup |
| `PROJECT_MANIFEST.md` | This file - complete file structure |

### In Each Service

Each microservice has:
- `pom.xml` - Maven configuration with dependencies
- `README.md` - Service-specific documentation
- `src/main/resources/application.properties` - Service configuration

---

## Java Source Code Organization

### Common Patterns Across All Services

1. **Controller Layer** (`controller/`)
   - REST endpoints
   - Request validation
   - Error handling delegation

2. **Service Layer** (`service/`)
   - Business logic
   - Database operations coordination
   - Inter-service calls

3. **Repository Layer** (`repository/`)
   - Spring Data JPA repositories
   - Custom query methods
   - Database abstraction

4. **Entity/Domain Layer** (`entity/` or `domain/`)
   - JPA entities
   - Database table mappings
   - Relationships

5. **DTO Layer** (`dto/`)
   - Request objects
   - Response objects
   - Data transfer objects

6. **Exception Handling**
   - Global `@ControllerAdvice`
   - Consistent error responses
   - HTTP status mapping

---

## Key Files by Category

### Authentication & Security

- `auth-service/src/main/java/com/example/authservice/security/SecurityConfig.java`
  - Spring Security configuration
  - JWT filter chain setup
  - CORS configuration

- `auth-service/src/main/java/com/example/authservice/security/JwtTokenProvider.java`
  - Token generation with HS512
  - Token validation
  - Claims extraction

- `auth-service/src/main/java/com/example/authservice/security/JwtAuthenticationFilter.java`
  - JWT extraction from requests
  - Bearer token parsing
  - Filter chain integration

### Business Logic

- `loan-application-service/.../LoanApplicationService.java`
  - Application workflow
  - Status transitions
  - Approval/rejection logic

- `loan-service/.../EMICalculator.java`
  - Financial EMI formula implementation
  - Schedule generation
  - Balance calculations

- `loan-service/.../RepaymentService.java`
  - Payment recording
  - Balance tracking
  - Payment history

- `report-service/.../ReportService.java`
  - Analytics calculations
  - Stream-based aggregations
  - Dashboard statistics

### Data Access

- `*/repository/*.java` - Spring Data JPA repositories with custom queries
- `*/domain/*.java` or `*/entity/*.java` - JPA entities representing tables

### API Contracts

- `*/dto/*.java` - Request/response DTOs for each endpoint
- `*/model/*.java` - Enums and constants

---

## Database Schema (Auto-Generated)

These tables are created automatically by Hibernate when services start:

| Table | Created By | Purpose |
|-------|-----------|---------|
| `users` | auth-service | User authentication and roles |
| `loan_applications` | loan-application-service | Loan applications with workflow |
| `loans` | loan-service | Approved loans |
| `emi_schedules` | loan-service | Monthly EMI payment schedules |
| `repayments` | loan-service | Payment transactions |
| `profiles` | profile-service | Customer KYC information |

---

## Configuration Files

### Maven POM Files

1. **Parent POM:** `backend-java/pom.xml`
   - Dependency management
   - Spring Boot/Cloud versions
   - Module definitions

2. **Service POMs:** `*/pom.xml`
   - Service-specific dependencies
   - Build plugins
   - Maven compiler settings

### Application Properties

Each service has: `src/main/resources/application.properties`

Configured for:
- Spring Boot settings
- Database connection
- Logging levels
- Service-specific configs (JWT, Feign, etc.)

---

## JAR Files (Build Artifacts)

After building, executable JARs are created at:

```
auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
loan-service/target/loan-service-0.0.1-SNAPSHOT.jar
loan-application-service/target/loan-application-service-0.0.1-SNAPSHOT.jar
profile-service/target/profile-service-0.0.1-SNAPSHOT.jar
report-service/target/report-service-0.0.1-SNAPSHOT.jar
```

Each can be run independently with:
```bash
java -jar service-name-0.0.1-SNAPSHOT.jar
```

---

## Frontend Placeholder

```
frontend/
└── (Separate Angular project - not included in this backend)
```

The frontend is a separate Angular 17+ project that communicates with the backend services via REST APIs documented in Swagger UI.

---

## Total Code Metrics

| Metric | Count |
|--------|-------|
| **Java Classes** | 80+ |
| **Lines of Production Code** | 3000+ |
| **REST Endpoints** | 30+ |
| **Database Tables** | 6 |
| **Maven Modules** | 6 (1 parent + 5 services) |
| **Configuration Files** | 5 (application.properties) |
| **Documentation Files** | 5 (Markdown guides) |
| **JAR Files (Compiled)** | 5 |

---

## Development & Build Workflow

### Build All Services

```bash
cd backend-java
mvn clean install -DskipTests
```

### Build Single Service

```bash
cd backend-java/auth-service
mvn clean install -DskipTests
```

### Run Service

```bash
java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar
```

### View API Documentation

Each service exposes Swagger UI at:
- http://localhost:8083/swagger-ui.html (Auth Service)
- http://localhost:8084/swagger-ui.html (Loan Application Service)
- http://localhost:8085/swagger-ui.html (Loan Service)
- http://localhost:8086/swagger-ui.html (Profile Service)
- http://localhost:8087/swagger-ui.html (Report Service)

---

## Quick Reference

### Port Assignments

| Service | Port | JAR Name |
|---------|------|----------|
| Auth Service | 8083 | auth-service-0.0.1-SNAPSHOT.jar |
| Loan Application Service | 8084 | loan-application-service-0.0.1-SNAPSHOT.jar |
| Loan Service | 8085 | loan-service-0.0.1-SNAPSHOT.jar |
| Profile Service | 8086 | profile-service-0.0.1-SNAPSHOT.jar |
| Report Service | 8087 | report-service-0.0.1-SNAPSHOT.jar |

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.3.5 |
| Language | Java 17 |
| Database | PostgreSQL + Hibernate |
| Authentication | JWT (JJWT 0.12.3) |
| API Docs | Springdoc OpenAPI 2.2.0 |
| IPC | Feign |

---

## File Size Information

Typical compiled JAR sizes (after mvn clean install):
- Each service JAR: ~60-80 MB (includes all dependencies)
- Build artifacts per service: ~150-200 MB (including source classes)

---

## Getting Started

1. **Read** `QUICK_START.md` for step-by-step instructions
2. **Review** `DELIVERY_SUMMARY.md` for feature overview
3. **Start** each service in separate terminal windows
4. **Test** using curl commands or Swagger UI
5. **Configure** using `CONFIGURATION_GUIDE.md` if needed

---

## Support Files

For additional help:
- **Build Issues:** See parent `pom.xml` and service-specific `pom.xml` files
- **Configuration:** See each service's `application.properties` and `CONFIGURATION_GUIDE.md`
- **API Usage:** See `QUICK_START.md` for examples
- **Implementation Details:** See service-specific `README.md` files

---

**Project Structure Last Updated:** 2025-12-30  
**Backend Version:** 0.0.1-SNAPSHOT  
**Java Version:** 17  
**Spring Boot Version:** 3.3.5
