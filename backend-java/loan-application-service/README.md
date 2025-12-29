# Loan Application Service (Java)

Spring Boot microservice for loan applications.

## Endpoints
Base path: `/api/loan-applications`

- POST `/apply` — submit a new application
  - Body: { userId, amount, termMonths, ratePercent? }
- GET `/my?userId=...` — list applications for a user
- GET `/{applicationId}` — get application
- PUT `/{applicationId}/review` — mark as UNDER_REVIEW

## Run
```bash
mvn -q -DskipTests spring-boot:run
```

Port: 8084, DB: PostgreSQL (`loans_db`).
