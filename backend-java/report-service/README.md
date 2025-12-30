# Report Service (Java)

Spring Boot microservice for generating reports and analytics across loan services.

## Endpoints
Base path: `/api/reports`

- GET `/loan-status` — get loans grouped by status with statistics
  - Returns: { statusDistribution, totalLoans, activeLoanCount, closedLoanCount }
- GET `/customer-summary` — get per-customer loan summary
  - Returns: [{ userId, totalLoans, totalLoanAmount, activeLoans, outstandingAmount }]
- GET `/dashboard` — get dashboard statistics
  - Returns: { totalLoans, pendingLoans, approvedLoans, rejectedLoans, totalLoanAmount, approvalRate }

## Features
- Aggregates data from loan-service using Feign clients
- Uses Java 8+ Streams for report calculations
- CORS enabled

## Run
```bash
cd report-service
mvn spring-boot:run
```

Port: 8087.
