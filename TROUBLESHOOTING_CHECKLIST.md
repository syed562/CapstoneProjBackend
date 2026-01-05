# Quick Troubleshooting Checklist

## Before Starting the Application

### Backend Startup
- [ ] **Eureka Server** is running on port 8761
  ```bash
  # Terminal 1
  cd backend-java/eureka-server
  mvn spring-boot:run
  ```

- [ ] **Config Server** is running (optional but recommended)
  ```bash
  # Terminal 2
  cd backend-java/config-server
  mvn spring-boot:run
  ```

- [ ] **API Gateway** is running on port 8080
  ```bash
  # Terminal 3
  cd backend-java/api-gateway
  mvn spring-boot:run
  ```

- [ ] **Auth Service** is running on port 8083
  ```bash
  # Terminal 4
  cd backend-java/auth-service
  mvn spring-boot:run
  ```

- [ ] **Loan Service** is running on port 8085
  ```bash
  # Terminal 5
  cd backend-java/loan-service
  mvn spring-boot:run
  ```

- [ ] **Loan Application Service** is running on port 8084
  ```bash
  # Terminal 6
  cd backend-java/loan-application-service
  mvn spring-boot:run
  ```

- [ ] **Other Services** (Profile, Notification, Report)
  ```bash
  # Profile Service (port 8082)
  # Notification Service (port 8086)
  # Report Service (port 8087)
  ```

### Frontend Startup
- [ ] **Node.js** is installed (version 18+)
  ```bash
  node --version
  npm --version
  ```

- [ ] **Angular CLI** is installed globally
  ```bash
  ng version
  ```

- [ ] **Dependencies** are installed
  ```bash
  cd frontend
  npm install
  ```

- [ ] Start frontend on port 4200
  ```bash
  npm start
  # or
  ng serve
  ```

## Verification Steps

### 1. Check Backend Services are Running

**Check Eureka Dashboard:**
```
Open browser: http://localhost:8761/
Look for all services listed (auth-service, loan-service, etc.)
```

**Check API Gateway Health:**
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{"status": "UP"}
```

### 2. Check CORS Configuration

**Test CORS Preflight Request:**
```bash
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: authorization" \
  -v
```

**Look for these headers in response:**
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
```

### 3. Check Frontend is Running

**Open browser:**
```
http://localhost:4200/
```

**Check browser console (F12):**
- No CORS errors
- No 404 errors for bundle files
- Angular version should be displayed

### 4. Test Login Flow

**Steps:**
1. Navigate to login page (http://localhost:4200/auth/login)
2. Enter test credentials
3. Click login
4. Check browser DevTools Network tab:
   - OPTIONS request should succeed (204 or 200)
   - POST request should succeed (200 or 201)
   - No CORS errors

**Expected Results:**
- Login succeeds → redirected to dashboard
- Token is stored in localStorage
- No error messages on screen

## Common Error Messages & Fixes

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Diagnosis:**
```bash
# Check if backend is running
curl http://localhost:8080/actuator/health

# Check CORS headers
curl -I http://localhost:8080/api/auth/login
```

**Fixes:**
1. Ensure API Gateway is running on port 8080
2. Verify CORS config includes `http://localhost:4200`
3. Rebuild backend to apply configuration changes:
   ```bash
   mvn clean install
   ```
4. Restart all services

### Error: "401 Unauthorized"

**Diagnosis:**
```bash
# Check token in browser
# Open DevTools → Application → Local Storage
# Look for "jwt_token" key
```

**Fixes:**
1. Try logging in again
2. Clear localStorage and refresh:
   ```javascript
   // In browser console
   localStorage.clear();
   location.reload();
   ```
3. Check backend JWT secret matches in all services
4. Verify token hasn't expired

### Error: "Cannot GET /api/loans"

**Diagnosis:**
```bash
# Check if loan-service is registered in Eureka
curl http://localhost:8761/eureka/apps/loan-service

# Check if route exists in API Gateway
curl -v http://localhost:8080/api/loans
```

**Fixes:**
1. Ensure loan-service is running and registered in Eureka
2. Wait 30 seconds for Eureka registration
3. Restart API Gateway after services register
4. Check `GatewayConfig.java` routes

### Error: "500 Internal Server Error"

**Diagnosis:**
```bash
# Check backend logs for stack trace
# Look at the specific service that's failing
# Example for loan-service error:
cat logs/loan-service.log | tail -50
```

**Fixes:**
1. Check backend console for exception details
2. Verify database is running (if needed)
3. Check RabbitMQ is running (if needed)
4. Verify all microservices are up
5. Check service configuration files

### Error: "Network Error" / "Service Unreachable"

**Diagnosis:**
```bash
# Test basic connectivity
ping localhost

# Check if port is open
netstat -an | grep 8080

# Test with curl
curl http://localhost:8080/api/auth/login -v
```

**Fixes:**
1. Ensure backend services are running
2. Check ports are not blocked by firewall
3. Verify no port conflicts
4. Use `BackendHealthService` to test:
   ```typescript
   // In browser console after Angular loads
   ng.probe(document.querySelector('app-root')).componentInstance.healthService.checkBackendHealth().subscribe(console.log)
   ```

## Log File Locations

**Backend Logs:**
```
backend-java/api-gateway/logs/
backend-java/auth-service/logs/
backend-java/loan-service/logs/
```

**Frontend Logs:**
- Check browser DevTools Console (F12)

## Clearing Cache & Rebuilding

### If Nothing Works, Try Nuclear Option:

**Backend:**
```bash
# Clean all microservices
cd backend-java
mvn clean install -DskipTests

# Rebuild individual service
cd api-gateway
mvn clean spring-boot:run
```

**Frontend:**
```bash
# Clear node_modules and reinstall
cd frontend
rm -rf node_modules package-lock.json
npm install

# Clear browser cache
# DevTools → Application → Clear site data

# Restart dev server
npm start
```

**Full Reset:**
```bash
# Kill all Java processes
pkill -f "java"

# Kill Node process
pkill -f "node"

# Wait 10 seconds
sleep 10

# Restart everything
```

## Validation Checklist

Use this when everything seems to work to make sure it really works:

- [ ] Frontend loads at http://localhost:4200
- [ ] No console errors in DevTools (F12)
- [ ] Can access login page
- [ ] Can submit login form
- [ ] Browser shows OPTIONS and POST requests in Network tab
- [ ] Both requests return 200/204 status
- [ ] No CORS errors in console
- [ ] Token appears in localStorage after login
- [ ] Can navigate to dashboard/profile pages
- [ ] Can load data from backend (loans, applications, etc.)
- [ ] Can perform create/update operations
- [ ] Error handling displays proper messages
- [ ] Logout clears token and redirects to login

## Still Having Issues?

### Collect Debug Information:

1. **Backend Service Status:**
   ```bash
   # Check all services registered
   curl http://localhost:8761/eureka/apps | grep "UP" | wc -l
   # Should show 6-8 services
   ```

2. **Frontend Environment:**
   ```javascript
   // In browser console
   console.log(ng.probe(document.querySelector('app-root')).componentInstance)
   ```

3. **Save Log Output:**
   ```bash
   # Capture backend logs
   mvn spring-boot:run > backend.log 2>&1
   
   # Capture frontend logs
   npm start > frontend.log 2>&1
   ```

4. **Check Specific Service:**
   ```bash
   # Test auth service directly (bypass gateway)
   curl http://localhost:8083/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'
   ```

### Common Ports Reference:
```
8761 - Eureka Server
8888 - Config Server
8080 - API Gateway
8083 - Auth Service
8082 - Profile Service
8084 - Loan Application Service
8085 - Loan Service
8086 - Notification Service
8087 - Report Service
4200 - Angular Frontend
```

## Quick Start Script

**For Windows (PowerShell):**
```powershell
# Start all services
cd backend-java
.\start-all-services.ps1

# In another terminal
cd frontend
npm start
```

**For Linux/Mac (Bash):**
```bash
# Terminal 1: Eureka
cd backend-java/eureka-server && mvn spring-boot:run &

# Terminal 2: Gateway
cd backend-java/api-gateway && mvn spring-boot:run &

# Terminal 3: Auth
cd backend-java/auth-service && mvn spring-boot:run &

# Terminal 4: Frontend
cd frontend && npm start &
```

---

**Last Updated:** 2025-01-05
**Status:** All fixes applied and tested
