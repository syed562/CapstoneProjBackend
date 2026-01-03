# Docker Deployment Guide

This guide explains how to deploy all microservices using Docker and Docker Compose.

## Prerequisites

- Docker Desktop installed (includes Docker Compose)
- Java 17 and Maven installed
- At least 8GB RAM allocated to Docker

## Quick Start

### 1. Build All Services

```powershell
# From backend-java directory
mvn clean package -DskipTests
```

This creates the JAR files needed for Docker images.

### 2. Start All Services

```powershell
# From backend-java directory
docker-compose up --build
```

This will:
- Build Docker images for all 9 microservices
- Start PostgreSQL and RabbitMQ containers
- Start services in the correct order:
  1. Config Server (port 8888)
  2. Eureka Server (port 8761)
  3. All microservices (auth, profile, loan, etc.)
  4. API Gateway (port 8080)

### 3. Verify Services

**Check Eureka Dashboard:**
```
http://localhost:8761
```
All services should be registered.

**Check RabbitMQ Management:**
```
http://localhost:15672
Username: guest
Password: guest
```

**Access API Gateway:**
```
http://localhost:8080
```

## Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Profile Service | 8082 |
| Auth Service | 8083 |
| Loan Application Service | 8084 |
| Loan Service | 8085 |
| Report Service | 8087 |
| Notification Service | 8088 |
| Config Server | 8888 |
| Eureka Server | 8761 |
| PostgreSQL | 5432 |
| RabbitMQ | 5672 |
| RabbitMQ Management | 15672 |

## Docker Commands

### Start Services (Detached Mode)
```powershell
docker-compose up -d
```

### Stop All Services
```powershell
docker-compose down
```

### Stop and Remove Volumes (Clean Start)
```powershell
docker-compose down -v
```

### View Logs

**All services:**
```powershell
docker-compose logs -f
```

**Specific service:**
```powershell
docker-compose logs -f auth-service
docker-compose logs -f loan-application-service
```

### Restart a Single Service
```powershell
docker-compose restart auth-service
```

### Rebuild a Single Service
```powershell
docker-compose up -d --build auth-service
```

### View Running Containers
```powershell
docker-compose ps
```

### Check Service Health
```powershell
docker-compose ps
docker inspect <container-name> --format='{{json .State.Health}}'
```

## Troubleshooting

### Services Not Starting

**Check logs:**
```powershell
docker-compose logs -f <service-name>
```

**Common issues:**
1. Config Server not ready - Wait 30-40 seconds for full startup
2. Database connection errors - Ensure PostgreSQL is healthy
3. Port conflicts - Ensure ports 8080-8088, 8761, 8888, 5432, 5672 are available

### Database Issues

**Connect to PostgreSQL:**
```powershell
docker exec -it loans-postgres psql -U postgres -d loans_db
```

**Check tables:**
```sql
\dt
SELECT * FROM users;
```

### RabbitMQ Issues

**Check queues:**
Visit http://localhost:15672 and check Queues tab.

### Rebuild After Code Changes

```powershell
# 1. Rebuild JARs
mvn clean package -DskipTests

# 2. Rebuild Docker images and restart
docker-compose up -d --build
```

## Networking

All services communicate through `microservices-network` bridge network. Services can reach each other using container names:
- `http://config-server:8888`
- `http://eureka-server:8761`
- `http://auth-service:8083`
- `jdbc:postgresql://postgres:5432/loans_db`
- `rabbitmq:5672`

## Environment Variables

Environment variables are configured in docker-compose.yml. Key variables:

**Database:**
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

**Eureka:**
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`

**Config Server:**
- `SPRING_CONFIG_IMPORT`

**RabbitMQ:**
- `SPRING_RABBITMQ_HOST`
- `SPRING_RABBITMQ_PORT`

## Production Considerations

1. **Secrets Management:** Use Docker secrets or external secret managers instead of plain-text passwords
2. **Resource Limits:** Add memory and CPU limits to docker-compose.yml
3. **Persistent Volumes:** PostgreSQL data is stored in `postgres-data` volume
4. **Scaling:** Use `docker-compose up --scale loan-service=3` to scale services
5. **Health Checks:** All critical services have health checks configured
6. **Restart Policy:** Services use `restart: on-failure` for automatic recovery

## Monitoring

**Check service health:**
```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8761/actuator/health
```

**Monitor resource usage:**
```powershell
docker stats
```

## Cleanup

**Remove all containers and volumes:**
```powershell
docker-compose down -v
```

**Remove all images:**
```powershell
docker rmi $(docker images -q 'backend-java*')
```
