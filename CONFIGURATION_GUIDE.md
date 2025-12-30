# Configuration Reference Guide

This guide explains all configuration options for the Loan Management System microservices.

---

## Common Configuration Properties

### Spring Boot Core

```properties
# Application name and port
spring.application.name=service-name
server.port=808X

# Environment
spring.profiles.active=dev
```

### Database Configuration

All services use the same PostgreSQL database with auto-schema creation:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/capstone_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection pooling
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

---

## Service-Specific Configuration

### 1. Auth Service (8083)

**Location:** `auth-service/src/main/resources/application.properties`

```properties
spring.application.name=auth-service
server.port=8083
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
app.jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation12345678
app.jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.example.authservice=DEBUG
```

**Key Environment Variables:**
- `APP_JWT_SECRET` - Change this in production to a secure random string (min 32 chars)
- `APP_JWT_EXPIRATION` - Token expiration in milliseconds (default: 24 hours = 86400000 ms)

**Security Configuration:**
- Algorithm: HS512 (HMAC with SHA-512)
- Token format: JWT (JSON Web Token)
- Password hashing: BCrypt with strength 12
- CORS: Enabled for localhost:4200, localhost:3000

---

### 2. Loan Application Service (8084)

**Location:** `loan-application-service/src/main/resources/application.properties`

```properties
spring.application.name=loan-application-service
server.port=8084
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.root=INFO
logging.level.com.example.loanapplication=DEBUG
```

**Features:**
- Workflow validation (status transitions)
- Remarks tracking for approvals/rejections
- User isolation (customers see only own applications)
- Admin access to all applications

---

### 3. Loan Service (8085)

**Location:** `loan-service/src/main/resources/application.properties`

```properties
spring.application.name=loan-service
server.port=8085
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.root=INFO
logging.level.com.example.loanservice=DEBUG
```

**Features:**
- EMI calculation (60-month schedule)
- Repayment tracking with transaction details
- Outstanding balance auto-calculation
- No external configuration needed

---

### 4. Profile Service (8086)

**Location:** `profile-service/src/main/resources/application.properties`

```properties
spring.application.name=profile-service
server.port=8086
spring.jpa.hibernate.ddl-auto=update

# Logging
logging.level.root=INFO
logging.level.com.example.profileservice=DEBUG
```

**Features:**
- Auto-creation of profile on first access
- KYC status management
- User-specific profile isolation

---

### 5. Report Service (8087)

**Location:** `report-service/src/main/resources/application.properties`

```properties
spring.application.name=report-service
server.port=8087

# Feign Client Configuration (for calling Loan Service)
feign.client.config.loan-service.connectTimeout=5000
feign.client.config.loan-service.readTimeout=5000

# Logging
logging.level.root=INFO
logging.level.com.example.reportservice=DEBUG
logging.level.feign=DEBUG
```

**Features:**
- Service-to-service communication via Feign
- Stream-based analytics calculations
- No database dependencies (read-only from Loan Service)

---

## Database Connection String Formats

### PostgreSQL (Default)
```
jdbc:postgresql://localhost:5432/capstone_db
```

### Remote PostgreSQL Server
```
jdbc:postgresql://prod-db.example.com:5432/capstone_db_prod
```

### With SSL
```
jdbc:postgresql://prod-db.example.com:5432/capstone_db?ssl=true&sslmode=require
```

---

## JWT Configuration

### Token Structure
```
Header.Payload.Signature

Header: {
  "alg": "HS512",
  "typ": "JWT"
}

Payload: {
  "sub": "user-id-uuid",
  "role": "CUSTOMER",
  "username": "customer1",
  "iat": 1704006000,
  "exp": 1704092400
}

Signature: HMAC-SHA512(header.payload, secret)
```

### Changing JWT Secret

**For Development:** Update in application.properties
```properties
app.jwt.secret=your-new-secret-key-at-least-32-chars-long-here
```

**For Production:** Use environment variable
```bash
export APP_JWT_SECRET="your-secure-production-secret-key-here"
```

### Token Expiration

Default: 24 hours (86400000 ms)

To change:
```properties
app.jwt.expiration=604800000  # 7 days
app.jwt.expiration=3600000    # 1 hour
```

---

## Security Configuration

### CORS (Cross-Origin Resource Sharing)

Currently allows:
- http://localhost:4200 (Angular default port)
- http://localhost:3000 (React/Node.js development)

To change, modify in `SecurityConfig.java`:
```java
.allowedOrigins("https://yourdomain.com", "https://app.yourdomain.com")
```

### Password Requirements

- Minimum length: 6 characters (configurable in DTOs)
- Hashing: BCrypt with strength 12
- Recommended: At least 8 chars with mixed case and numbers

### Authorization Roles

| Role | Access | Endpoints |
|------|--------|-----------|
| **CUSTOMER** | Own data | Apply for loan, view profile, make payments |
| **LOAN_OFFICER** | All applications | Review, approve, reject applications |
| **ADMIN** | All data | User management, system configuration |

---

## Logging Configuration

### Log Levels

```properties
# Global level
logging.level.root=INFO

# Service-specific
logging.level.com.example.authservice=DEBUG
logging.level.com.example.loanservice=DEBUG
logging.level.com.example.reportservice=DEBUG

# Framework levels
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=INFO
logging.level.org.springframework.data=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Log Output

By default, logs go to console. To write to file:

```properties
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

---

## Feign Client Configuration

Used by Report Service to call Loan Service:

```properties
# Connection and read timeout
feign.client.config.loan-service.connectTimeout=5000
feign.client.config.loan-service.readTimeout=5000

# Logging
feign.client.config.loan-service.loggerLevel=full

# Global Feign configuration
feign.compression.request.enabled=true
feign.compression.response.enabled=true
```

---

## Hibernate Configuration

### DDL Strategy

```properties
# Update: Create tables if missing, don't drop existing (RECOMMENDED for development)
spring.jpa.hibernate.ddl-auto=update

# Create: Drop all tables and recreate (Use for testing only)
spring.jpa.hibernate.ddl-auto=create

# Validate: Check if schema matches entities (Use in production)
spring.jpa.hibernate.ddl-auto=validate

# None: Don't touch database (Use in production with migrations)
spring.jpa.hibernate.ddl-auto=none
```

### Performance Tuning

```properties
# Show SQL statements in log
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Batch size for inserts/updates
spring.jpa.properties.hibernate.jdbc.batch_size=20

# Connection timeout
spring.datasource.hikari.connection-timeout=20000

# Idle timeout
spring.datasource.hikari.idle-timeout=600000
```

---

## Environment-Based Configuration

### Development (Default)

```bash
# Run with default development settings
java -jar service-name-0.0.1-SNAPSHOT.jar

# Or explicitly set profile
java -jar service-name-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Production

Create `application-prod.properties` with production-specific settings:

```properties
spring.datasource.url=jdbc:postgresql://prod-db.example.com:5432/capstone_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000

logging.level.root=WARN
spring.jpa.hibernate.ddl-auto=validate

# Security
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

Run with:
```bash
java -jar service-name-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

---

## Troubleshooting Configuration Issues

### Issue: Cannot connect to database

**Solution:**
```bash
# Verify PostgreSQL is running
psql -U postgres -c "SELECT version();"

# Check connection string in application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/capstone_db
```

### Issue: JWT token invalid/expired

**Solution:**
```properties
# Increase expiration time (in milliseconds)
app.jwt.expiration=604800000  # 7 days instead of 24 hours

# Or verify secret key is consistent across services
app.jwt.secret=same-secret-used-for-token-generation
```

### Issue: CORS error from frontend

**Solution:** Update SecurityConfig.java to allow your frontend domain:
```java
.allowedOrigins("https://yourdomain.com")
```

### Issue: Slow database queries

**Solution:** Add database connection pooling settings:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.jpa.properties.hibernate.jdbc.batch_size=20
```

### Issue: OutOfMemory error

**Solution:** Increase heap memory when starting service:
```bash
java -Xms512m -Xmx2048m -jar service-name-0.0.1-SNAPSHOT.jar
```

---

## Performance Tuning Checklist

- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` in production
- [ ] Increase Hikari pool size based on expected connections
- [ ] Enable Hibernate batch processing (batch_size=20)
- [ ] Use connection caching for Feign clients
- [ ] Set appropriate JWT expiration time
- [ ] Monitor database logs with `logging.level.org.hibernate=DEBUG`
- [ ] Use pagination for large result sets
- [ ] Enable Gzip compression for API responses
- [ ] Set appropriate log levels (WARN for production)

---

## Securing Configuration in Production

**Recommended practices:**

1. **Use environment variables for sensitive data:**
   ```bash
   export DB_PASSWORD="secure-password"
   export JWT_SECRET="secure-jwt-key"
   export DATASOURCE_URL="jdbc:postgresql://prod-db:5432/capstone"
   ```

2. **Never commit secrets to version control:**
   ```
   .gitignore
   application-prod.properties
   *.key
   *.pem
   ```

3. **Use Spring Cloud Config Server for centralized configuration**

4. **Enable HTTPS:**
   ```properties
   server.ssl.key-store=classpath:keystore.jks
   server.ssl.key-store-password=${KEYSTORE_PASSWORD}
   server.ssl.key-store-type=JKS
   ```

5. **Restrict database access** - Only application servers should access database

6. **Rotate JWT secrets periodically** in production

---

## Configuration Validation

Check your configuration is correct:

```bash
# View active properties
curl http://localhost:8083/actuator/env

# Check service is running
curl http://localhost:8083/actuator/health

# Get application name
curl http://localhost:8083/actuator/info
```

(Requires Spring Boot Actuator - add to dependencies if needed)

---

**For more information, refer to the Spring Boot official documentation:**
https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
