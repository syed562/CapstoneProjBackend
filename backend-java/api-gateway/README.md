# API Gateway

Single entry point for all microservices in the Loan Management System.

## Port
- **8080** (Gateway entry point)

## Routes

All requests should go through: `http://localhost:8080`

### Authentication
- `POST /api/auth/login` → Auth Service (8083)
- `POST /api/auth/register` → Auth Service (8083)

### Profiles
- `GET /api/profiles/me` → Profile Service (8082)
- `PUT /api/profiles/me` → Profile Service (8082)
- `GET /api/profiles/{userId}` → Profile Service (8082)

### Loan Applications
- `POST /api/loan-applications/apply` → Loan Application Service (8084)
- `GET /api/loan-applications` → Loan Application Service (8084)
- `PUT /api/loan-applications/{id}/approve` → Loan Application Service (8084)

### Loans
- `GET /api/loans` → Loan Service (8085)
- `GET /api/loans/my` → Loan Service (8085)
- `POST /api/loans/from-application` → Loan Service (8085)

### Payments
- `POST /api/payments/record` → Loan Service (8085)
- `GET /api/payments/loan/{loanId}` → Loan Service (8085)

### Reports
- `GET /api/reports/dashboard` → Report Service (8087)
- `GET /api/reports/comprehensive` → Report Service (8087)

## Features
- **CORS Handling**: Configured for all origins
- **Route-based Routing**: Automatic routing based on path
- **Health Checks**: Available at `/actuator/health`
- **Gateway Routes Info**: Available at `/actuator/gateway/routes`

## Running

```bash
mvn spring-boot:run
```

## Testing Gateway

```bash
# Test auth through gateway
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@example.com","password":"password"}'

# Test loans through gateway
curl http://localhost:8080/api/loans \
  -H "Authorization: Bearer <token>"
```
