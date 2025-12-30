# Auth Service (Java)

Spring Boot microservice for user authentication and authorization using JWT.

## Endpoints
Base path: `/api/auth`

- POST `/login` — authenticate user and get JWT token
  - Body: { username, password }
  - Returns: { token, userId, username, role }
- POST `/register` — create new user account
  - Body: { username, email, password, role? }
  - Returns: { userId, username, email, role }
- GET `/users/{userId}` — get user details (requires authentication)

## Features
- JWT-based authentication
- BCrypt password encoding
- Role-based access control (ADMIN, LOAN_OFFICER, CUSTOMER)
- User registration and login
- CORS enabled

## Run
```bash
cd auth-service
mvn spring-boot:run
```

Port: 8083, DB: PostgreSQL (`loans_db`).
