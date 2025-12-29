# Loan Service (Java)

Spring Boot microservice providing CRUD endpoints for loans.

## Endpoints
- GET /loans — list loans
- GET /loans/{id} — get loan
- POST /loans — create loan
- PATCH /loans/{id}/status — update status
- DELETE /loans/{id} — delete loan

In-memory storage for now. CORS enabled.

## Prereqs
- JDK 17+
- Maven 3.8+

## Run
```bash
mvn spring-boot:run
```

Listens on port 8082.
