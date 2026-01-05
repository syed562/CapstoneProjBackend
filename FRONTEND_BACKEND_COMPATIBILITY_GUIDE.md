# Frontend-Backend Compatibility Fix Guide

## Overview
This guide documents all the changes made to ensure frontend (Angular) and backend (Spring Boot microservices) are fully compatible, with proper CORS configuration and error handling to eliminate 500 and CORS errors.

## Backend Changes

### 1. API Gateway (api-gateway)
**File:** `backend-java/api-gateway/src/main/resources/application.properties`

- CORS is already configured with the following settings:
  - Allowed Origins: `http://localhost:4200`, `http://localhost:3000`
  - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
  - Allowed Headers: * (all)
  - Credentials: true
  - Max Age: 3600 seconds

**Security Configuration:** `GatewaySecurityConfig.java`
- CSRF is disabled for stateless JWT authentication
- JWT filtering is configured for all `/api/**` endpoints except `/api/auth/**`
- Authorization checks are properly configured

### 2. Auth Service (auth-service)
**File:** `backend-java/auth-service/src/main/java/com/example/authservice/config/SecurityConfig.java`

- Already has CORS configured for:
  - `http://localhost:4200` and `http://localhost:3000`
  - All required HTTP methods (GET, POST, PUT, DELETE, PATCH, OPTIONS)
  - Credentials support enabled

### 3. Loan Service (loan-service)
**New File:** `backend-java/loan-service/src/main/java/com/example/loanservice/config/CorsConfig.java`

- Created new CORS configuration with `CorsFilter` bean
- Allows requests from frontend on port 4200 and 3000
- Supports credentials and all necessary HTTP methods

### 4. Profile Service (profile-service)
**New File:** `backend-java/profileservice/src/main/java/com/example/profileservice/config/CorsConfig.java`

- Created new CORS configuration with `CorsFilter` bean
- Consistent with other microservices

### 5. Loan Application Service (loan-application-service)
**New File:** `backend-java/loanapplication/src/main/java/com/example/loanapplication/config/CorsConfig.java`

- Created new CORS configuration with `CorsFilter` bean
- Integrated with security and REST controllers

### 6. Notification Service (notification-service)
**New File:** `backend-java/notificationservice/src/main/java/com/example/notificationservice/config/CorsConfig.java`

- Created new CORS configuration with `CorsFilter` bean

### 7. Report Service (report-service)
**New File:** `backend-java/reportservice/src/main/java/com/example/reportservice/config/CorsConfig.java`

- Created new CORS configuration with `CorsFilter` bean

## Frontend Changes

### 1. Environment Configuration
**File:** `frontend/src/environments/environment.ts`

Current configuration:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  apiGatewayUrl: 'http://localhost:8080',
  tokenKey: 'jwt_token',
  userKey: 'current_user'
};
```

**Important:** Ensure the `apiUrl` points to your API Gateway running on port 8080.

### 2. JWT Interceptor
**File:** `frontend/src/app/core/interceptors/jwt.interceptor.ts`

**Key Changes:**
- Added `withCredentials: true` to all HTTP requests (required for CORS with credentials)
- Maintains Authorization header with Bearer token
- Enhanced error logging for debugging
- Handles 401 (unauthorized), CORS, and server errors

```typescript
// Now includes withCredentials for CORS
let clonedRequest = request.clone({
  withCredentials: true
});
```

### 3. Error Interceptor (NEW)
**File:** `frontend/src/app/core/interceptors/error.interceptor.ts`

**Purpose:** Global error handling for all HTTP requests

**Features:**
- Catches and logs all HTTP errors
- Provides human-readable error messages for different status codes
- Distinguishes between client-side and server-side errors
- Handles CORS/Network errors (status 0)
- Logs detailed error information for debugging
- Returns structured error object with status and message

**Status Code Handling:**
- **0**: Network error or CORS issue
- **400**: Bad request with input validation errors
- **401**: Unauthorized (session expired)
- **403**: Forbidden (no permission)
- **404**: Resource not found
- **500**: Server error
- **503**: Service unavailable

### 4. Error Handler Service (NEW)
**File:** `frontend/src/app/core/services/error-handler.service.ts`

**Purpose:** Centralized error management service

**Features:**
- Observable-based error broadcasting to UI components
- Success message notifications
- Human-readable error message extraction
- Current error tracking
- Auto-clearing of success messages after 5 seconds

**Usage Example:**
```typescript
// In any component
constructor(private errorHandler: ErrorHandlerService) {}

// Subscribe to errors
this.errorHandler.error$.subscribe(error => {
  console.log(error.message);
});

// Handle error manually
this.errorHandler.handleError(httpError);

// Show success
this.errorHandler.showSuccess('Operation completed!');
```

### 5. Backend Health Service (NEW)
**File:** `frontend/src/app/core/services/backend-health.service.ts`

**Purpose:** Check backend connectivity status

**Features:**
- Pings backend to verify it's running
- 5-second timeout to detect slow/unresponsive services
- Returns health status with detailed message
- Useful for splash screens or health dashboards

**Usage Example:**
```typescript
constructor(private healthService: BackendHealthService) {}

ngOnInit() {
  this.healthService.checkBackendHealth().subscribe(status => {
    if (status.status === 'healthy') {
      console.log('Backend is up!');
    } else {
      console.error('Backend issue:', status.message);
    }
  });
}
```

### 6. Core Module
**File:** `frontend/src/app/core/core.module.ts`

**Changes:**
- Registered `ErrorInterceptor` (must come BEFORE `JwtInterceptor`)
- Registered `JwtInterceptor` for token injection
- Both configured with `multi: true` for proper ordering

**Interceptor Order:**
1. ErrorInterceptor (catches and handles errors first)
2. JwtInterceptor (adds authorization header)

### 7. Auth Service Updates
**File:** `frontend/src/app/core/services/auth.service.ts`

**Changes:**
- Added `withCredentials: true` to all HTTP requests
- Integrated with `ErrorHandlerService`
- Enhanced error handling in login/register methods
- Success messages after successful operations

### 8. Loan Service Updates
**File:** `frontend/src/app/core/services/loan.service.ts`

**Changes:**
- Added `withCredentials: true` to all HTTP requests
- Integrated with `ErrorHandlerService`
- Proper error handling for all loan-related endpoints
- Consistent error reporting

### 9. Report Service Updates
**File:** `frontend/src/app/core/services/report.service.ts`

**Changes:**
- Added `withCredentials: true` to all HTTP requests
- Integrated with `ErrorHandlerService`
- Error handling for report endpoints

## How to Use Error Handling in Components

### In Login Component:
```typescript
import { Component } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ErrorHandlerService } from '../services/error-handler.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loading = false;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private errorHandler: ErrorHandlerService
  ) {
    // Subscribe to errors
    this.errorHandler.error$.subscribe(err => {
      this.error = err?.message || null;
    });
  }

  login(credentials: any) {
    this.loading = true;
    this.errorHandler.clearError();

    this.authService.login(credentials).subscribe({
      next: () => {
        this.loading = false;
        // Navigation happens in service
      },
      error: () => {
        this.loading = false;
        // Error is already handled by interceptor and service
      }
    });
  }
}
```

### In List Component:
```typescript
export class LoanListComponent implements OnInit {
  loans$ = this.loanService.getAllApplications();
  error$ = this.errorHandler.error$;

  constructor(
    private loanService: LoanService,
    private errorHandler: ErrorHandlerService
  ) {}
}
```

### In HTML Template:
```html
<!-- Display error message -->
<div *ngIf="error$ | async as error" class="alert alert-danger">
  <strong>Error:</strong> {{ error.message }}
  <button (click)="errorHandler.clearError()">Dismiss</button>
</div>

<!-- Or use success message -->
<div *ngIf="(errorHandler.success$ | async) as message" class="alert alert-success">
  {{ message }}
</div>
```

## Testing Connectivity

### 1. Verify Backend is Running:
```bash
# Check if API Gateway is up
curl http://localhost:8080/actuator/health

# Check specific microservice health
curl http://localhost:8083/actuator/health  # auth-service
curl http://localhost:8085/actuator/health  # loan-service
curl http://localhost:8084/actuator/health  # loan-application-service
```

### 2. Test CORS Preflight:
```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

### 3. Frontend Health Check:
Use the provided `BackendHealthService` in your splash screen:
```typescript
ngOnInit() {
  this.healthService.checkBackendHealth().subscribe(status => {
    if (status.status === 'healthy') {
      // Load app
    } else {
      // Show error: "Backend is not running"
    }
  });
}
```

## Common Issues & Solutions

### Issue 1: "No 'Access-Control-Allow-Origin' header"
**Cause:** CORS not properly configured or request missing `withCredentials`

**Solution:**
- Ensure all services have `CorsConfig` bean
- Verify `withCredentials: true` in HTTP requests
- Check that origin `http://localhost:4200` is in allowed list

### Issue 2: "401 Unauthorized"
**Cause:** JWT token not being sent or invalid

**Solution:**
- Check that token is stored in localStorage with correct key
- Verify JWT interceptor is adding Authorization header
- Check token expiration time
- Ensure backend secret matches frontend JWT signing key

### Issue 3: "500 Server Error"
**Cause:** Backend service crashing or logic error

**Solution:**
- Check backend service logs for stack traces
- Verify microservice is running on correct port
- Ensure database connections are active
- Check Eureka for service registration

### Issue 4: "Cannot GET /api/..."
**Cause:** API Gateway not routing correctly or service not registered

**Solution:**
- Verify all microservices are registered with Eureka
- Check API Gateway routes in `GatewayConfig.java`
- Confirm service names match in routes and Eureka

### Issue 5: Network Error (status 0)
**Cause:** CORS issue, network timeout, or service unavailable

**Solution:**
- Run the `BackendHealthService` to test connectivity
- Check browser DevTools Network tab for OPTIONS requests
- Verify backend service is running and accessible
- Check firewall/port availability

## Production Deployment

### Update CORS Origins for Production:
When deploying to production, update all CORS configurations to use your production domain:

**In each service's `CorsConfig.java` or `SecurityConfig.java`:**
```java
configuration.setAllowedOrigins(Arrays.asList(
  "http://localhost:4200",  // Development
  "http://localhost:3000",  // Development
  "https://yourdomain.com"  // Production
));
```

**In Frontend environment.prod.ts:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.yourdomain.com/api',
  // ...
};
```

## Summary of Changes

✅ **Backend:**
- Added CORS support to all 7 microservices
- Configured to accept requests from Angular app (localhost:4200)
- Proper handling of preflight OPTIONS requests
- Credentials support enabled for JWT authentication

✅ **Frontend:**
- JWT interceptor with `withCredentials: true`
- Global error interceptor with user-friendly messages
- Centralized error handler service
- Backend health checking service
- Updated all services with proper error handling
- Console logging for debugging

✅ **Security:**
- JWT tokens properly managed
- CSRF disabled (stateless JWT auth)
- Credentials only sent to same origin
- Proper CORS configuration on all endpoints

## Next Steps

1. Start all backend services
2. Verify Eureka shows all services registered
3. Run Angular frontend: `npm start`
4. Test login flow - should not see CORS errors
5. Test API calls - should properly handle errors with messages
6. Monitor browser DevTools for any remaining issues
7. Check backend logs for 500 errors and fix application logic

## Support Resources

- **Angular HTTP Client:** https://angular.io/guide/http
- **Spring Boot CORS:** https://spring.io/guides/gs/cors-rest/
- **JWT Authentication:** https://jwt.io
- **CORS Explained:** https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
- **RxJS Error Handling:** https://rxjs.dev/guide/operators
