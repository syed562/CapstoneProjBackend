# Swagger UI Integration Guide

This document explains the Swagger/OpenAPI integration for the Loan Management System microservices architecture.

## Overview

Swagger UI has been integrated into all microservices using **SpringDoc OpenAPI** to provide interactive API documentation. You can access the Swagger UI through individual services or via the centralized API Gateway.

## What's Been Configured

### 1. Dependencies Added

All microservices now have the SpringDoc OpenAPI dependency:

**For standard services (auth, loan, profile, notification, report, loan-application):**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

**For API Gateway (WebFlux):**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. Configuration Files

Each service has:
- **SwaggerConfig.java** - OpenAPI configuration with service metadata, JWT authentication scheme
- **application.properties** - SpringDoc settings for API docs and Swagger UI paths

### 3. Security Configuration

Security configurations have been updated to allow access to Swagger endpoints:
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/api-docs`
- `/v3/api-docs/**`

## Accessing Swagger UI

### Option 1: Via API Gateway (Recommended)

Access all services from a single centralized Swagger UI:

**URL:** http://localhost:8080/swagger-ui.html

This provides a dropdown to switch between different services:
- Auth Service
- Loan Service
- Loan Application Service
- Profile Service
- Notification Service
- Report Service

### Option 2: Direct Service Access

Access each service's Swagger UI individually:

| Service | Port | Swagger UI URL | API Docs URL |
|---------|------|----------------|--------------|
| API Gateway | 8080 | http://localhost:8080/swagger-ui.html | - |
| Auth Service | 8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |
| Profile Service | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Loan Service | 8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/api-docs |
| Loan Application Service | 8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/api-docs |
| Notification Service | 8088 | http://localhost:8088/swagger-ui.html | http://localhost:8088/api-docs |
| Report Service | 8087 | http://localhost:8087/swagger-ui.html | http://localhost:8087/api-docs |

### Option 3: Via API Gateway Routes

Access individual service Swagger UIs through the gateway:

```
http://localhost:8080/auth-service/swagger-ui.html
http://localhost:8080/loan-service/swagger-ui.html
http://localhost:8080/loan-application-service/swagger-ui.html
http://localhost:8080/profile-service/swagger-ui.html
http://localhost:8080/notification-service/swagger-ui.html
http://localhost:8080/report-service/swagger-ui.html
```

## Using JWT Authentication in Swagger

Most API endpoints require JWT authentication. Here's how to use it in Swagger:

1. **Get a JWT Token:**
   - Navigate to Auth Service endpoints
   - Use `/api/auth/login` or `/api/auth/register`
   - Execute the request and copy the JWT token from the response

2. **Authorize in Swagger:**
   - Click the **"Authorize"** button (lock icon) at the top right
   - Paste your JWT token in the "Value" field
   - Click "Authorize"
   - Click "Close"

3. **Make Authenticated Requests:**
   - All subsequent API calls will include the JWT token
   - The token is sent in the `Authorization: Bearer <token>` header

## Features

### API Documentation Includes:
- ✅ Complete endpoint descriptions
- ✅ Request/Response schemas
- ✅ Authentication requirements
- ✅ HTTP methods and status codes
- ✅ Try-it-out functionality
- ✅ JWT Bearer token authentication
- ✅ Server URL selection (local vs gateway)

### Configuration Options:
- Operations sorted by HTTP method
- Tags sorted alphabetically
- Actuator endpoints excluded from docs
- JWT security scheme pre-configured

## Starting the Services

To use Swagger UI, ensure all services are running:

```powershell
# Start all services
cd backend-java
.\start-all-services.ps1

# Or start with API Gateway
.\start-with-gateway.ps1
```

Make sure:
1. ✅ Eureka Server is running (http://localhost:8761)
2. ✅ All microservices are registered with Eureka
3. ✅ API Gateway is running (http://localhost:8080)

## Customization

### Modify Service Information

Edit the `SwaggerConfig.java` file in each service to update:
- Title
- Description
- Version
- Contact information
- License information

Example:
```java
.info(new Info()
    .title("Your Service Name")
    .description("Your service description")
    .version("2.0.0")
    .contact(new Contact()
        .name("Your Team")
        .email("your-email@example.com")))
```

### Add Custom API Documentation

Use OpenAPI annotations on your controllers:

```java
@RestController
@RequestMapping("/api/example")
@Tag(name = "Example", description = "Example API endpoints")
public class ExampleController {
    
    @Operation(
        summary = "Get example by ID",
        description = "Returns a single example object by ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
        }
    )
    @GetMapping("/{id}")
    public Example getExample(@PathVariable Long id) {
        // implementation
    }
}
```

## Troubleshooting

### Swagger UI Not Loading
- Verify the service is running
- Check security configuration allows Swagger endpoints
- Ensure correct port number

### API Docs Not Showing
- Check `springdoc.api-docs.path` in application.properties
- Verify the service is properly registered with Eureka
- Check gateway routing configuration

### Authentication Errors
- Ensure JWT token is valid and not expired
- Check the token is properly authorized in Swagger UI
- Verify JWT secret matches between services

### Gateway Aggregation Not Working
- Ensure all services are registered with Eureka
- Check gateway routes in `GatewayConfig.java`
- Verify `springdoc.swagger-ui.urls` in gateway application.properties

## Next Steps

1. **Rebuild Services:** Rebuild each service to include the new dependencies
   ```powershell
   cd backend-java
   mvn clean install
   ```

2. **Restart Services:** Restart all services to load the new configuration

3. **Test Swagger UI:** Access the API Gateway Swagger UI and explore the APIs

4. **Document Your APIs:** Add OpenAPI annotations to your controllers for better documentation

## API Docs Export

You can export the OpenAPI specification in JSON format:
- Via Gateway: http://localhost:8080/auth-service/api-docs
- Direct: http://localhost:8083/api-docs

This JSON can be used with:
- Postman (import collection)
- Code generation tools
- API documentation portals
- Third-party integration tools

## Benefits

✅ **Interactive Testing:** Test APIs directly from the browser  
✅ **Centralized Documentation:** All APIs documented in one place  
✅ **Authentication Support:** Built-in JWT token handling  
✅ **Developer Friendly:** Easy to explore and understand APIs  
✅ **Auto-Generated:** Documentation stays in sync with code  
✅ **Export Capabilities:** Generate client SDKs and collections  

## Resources

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Guide](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/)
