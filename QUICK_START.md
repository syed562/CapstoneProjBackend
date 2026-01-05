# Quick Start Guide - After Compatibility Fix

## What Was Fixed

✅ **CORS Errors** - Added CORS configuration to all 6 microservices  
✅ **500 Errors** - Added global error handling with user-friendly messages  
✅ **Authentication Issues** - Fixed JWT token transmission with credentials  
✅ **Request Failures** - Added proper request/response interceptors  

---

## 5-Minute Startup Guide

### Prerequisites
- [ ] Java 17+ installed
- [ ] Maven 3.8+ installed
- [ ] Node.js 18+ installed
- [ ] npm or yarn installed

### Step 1: Start Backend Services (Terminal 1-7)

**Terminal 1 - Eureka Server (Port 8761)**
```bash
cd backend-java/eureka-server
mvn spring-boot:run
```
Wait for: `Tomcat started on port(s): 8761`

**Terminal 2 - API Gateway (Port 8080)**
```bash
cd backend-java/api-gateway
mvn spring-boot:run
```
Wait for: `Tomcat started on port(s): 8080`

**Terminal 3 - Auth Service (Port 8083)**
```bash
cd backend-java/auth-service
mvn spring-boot:run
```

**Terminal 4 - Loan Service (Port 8085)**
```bash
cd backend-java/loan-service
mvn spring-boot:run
```

**Terminal 5 - Loan Application Service (Port 8084)**
```bash
cd backend-java/loan-application-service
mvn spring-boot:run
```

**Terminal 6 - Profile Service (Port 8082)**
```bash
cd backend-java/profile-service
mvn spring-boot:run
```

**Terminal 7 - Other Services** (Optional)
```bash
# Notification Service (Port 8086)
cd backend-java/notification-service && mvn spring-boot:run

# Report Service (Port 8087)
cd backend-java/report-service && mvn spring-boot:run
```

### Step 2: Verify Backend is Ready

**Check Eureka Dashboard:**
Open http://localhost:8761 in browser

Should see:
- auth-service (1 instance)
- loan-service (1 instance)
- api-gateway (1 instance)
- And other services

All should show **UP** status.

### Step 3: Start Frontend (Terminal 8)

```bash
cd frontend
npm install  # (only needed first time)
npm start
```

Wait for: `Application bundle generation complete`

### Step 4: Open Application

Open http://localhost:4200 in browser

You should see:
- ✅ No CORS errors
- ✅ Login page loads
- ✅ Network requests visible in DevTools

---

## Testing the Fix

### Test 1: Check CORS is Working

**In Browser DevTools (F12):**
1. Go to Network tab
2. Try to login
3. Look for OPTIONS request
4. Should see:
   - Status: 204 or 200 (not red)
   - Response Headers have: `Access-Control-Allow-Origin: http://localhost:4200`

### Test 2: Check Error Handling

**Try a failed login:**
1. Enter wrong username
2. Should see error message on screen (not browser error)
3. Error message explains what went wrong

### Test 3: Check Token Handling

**After successful login:**
1. Open DevTools → Application → Local Storage
2. Look for `jwt_token` key
3. Value should be a long string (JWT token)
4. Check `current_user` has your user info

### Test 4: Check Protected Endpoints

**After login:**
1. Navigate to any page that loads data
2. Should load successfully
3. Network tab should show GET requests with Authorization header

---

## Troubleshooting Quick Fixes

### "CORS policy: No 'Access-Control-Allow-Origin'"

**Fix:**
1. Verify all backend services are running
2. Check Eureka shows all services as UP
3. Restart API Gateway:
   ```bash
   # Kill terminal running API Gateway
   # Then restart it
   ```
4. Hard refresh browser (Ctrl+Shift+R)

### "Cannot read properties of null (reading 'Authorization')"

**Fix:**
1. Make sure auth-service is running
2. Check auth-service logs for errors
3. Try login again

### "Network Error" or "Failed to fetch"

**Fix:**
1. Verify API Gateway is running on port 8080
2. Run: `curl http://localhost:8080/actuator/health`
3. Should return: `{"status":"UP"}`
4. Restart API Gateway if needed

### "401 Unauthorized after login"

**Fix:**
1. Clear browser storage: `localStorage.clear()` in console
2. Logout and login again
3. Check that token is being stored:
   ```javascript
   // In browser console
   console.log(localStorage.getItem('jwt_token'));
   ```

### "Cannot GET /api/..."

**Fix:**
1. Check Eureka - service should be registered
2. Wait 30 seconds for Eureka registration
3. Check the service is actually running
4. Restart API Gateway to pick up new routes

---

## Common Ports Reference

```
Eureka Server:             http://localhost:8761
API Gateway:              http://localhost:8080
Auth Service:             http://localhost:8083
Profile Service:          http://localhost:8082
Loan Application Service: http://localhost:8084
Loan Service:             http://localhost:8085
Notification Service:     http://localhost:8086
Report Service:           http://localhost:8087
Frontend (Angular):       http://localhost:4200
```

---

## Useful Commands

### Check if Port is in Use
```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080
```

### Kill Process on Port
```bash
# Windows
taskkill /F /PID <process_id>

# Linux/Mac
kill -9 <process_id>
```

### Clear Frontend Build Cache
```bash
cd frontend
rm -rf dist node_modules package-lock.json
npm install
npm start
```

### Check Backend Health
```bash
curl http://localhost:8080/actuator/health -v
```

### Test API Directly
```bash
# Login request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass"}'

# With token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl http://localhost:8080/api/loans \
  -H "Authorization: Bearer $TOKEN"
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  Browser (Angular Frontend)                             │
│  http://localhost:4200                                  │
│  ├─ Error Interceptor (handles all errors)             │
│  └─ JWT Interceptor (adds auth header)                 │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP Requests (CORS enabled!)
                     ▼
┌─────────────────────────────────────────────────────────┐
│  API Gateway                                             │
│  http://localhost:8080                                  │
│  ├─ Routes to microservices                            │
│  └─ CORS filtering                                      │
└──────┬──────────┬──────────┬──────────┬──────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐
   │Auth    │ │Loan    │ │Profile │ │Others    │
   │Service │ │Service │ │Service │ │Services  │
   │:8083   │ │:8085   │ │:8082   │ │:8084-87  │
   └────────┘ └────────┘ └────────┘ └──────────┘
       │          │          │          │
       └─────┬────┴─────┬────┴─────┬────┘
             │          │          │
             ▼          ▼          ▼
        ┌─────────────────────────────────┐
        │  Service Registry (Eureka)      │
        │  http://localhost:8761          │
        └─────────────────────────────────┘
```

---

## Files That Changed

### Backend
- ✅ 6 new CORS config files (one per microservice)
- ✅ Existing security configs already proper

### Frontend
- ✅ 1 updated JWT interceptor
- ✅ 1 new Error interceptor
- ✅ 1 updated Core module
- ✅ 3 updated services
- ✅ 2 new services

### Documentation
- ✅ Comprehensive compatibility guide
- ✅ Troubleshooting checklist
- ✅ API examples
- ✅ This quick start guide

---

## Success Indicators

When everything is working:

✅ Frontend loads without errors  
✅ Can see login page  
✅ Can submit login form  
✅ Network tab shows OPTIONS + POST (both succeed)  
✅ No CORS errors in console  
✅ No red errors in Network tab  
✅ Token appears in localStorage after login  
✅ Can navigate to protected pages  
✅ Can load data from backend  
✅ Error messages are user-friendly  

---

## Next Steps

1. **Start all services** (follow Startup Guide above)
2. **Test login** (try the test credentials)
3. **Check Network tab** (verify CORS works)
4. **Try some operations** (create, update, list)
5. **Check error handling** (try invalid actions)

If you hit any issues:
1. Check `TROUBLESHOOTING_CHECKLIST.md`
2. Review logs in backend service terminals
3. Check browser DevTools Network tab
4. Use curl to test API directly

---

## Performance Tips

### Frontend
```bash
# Build for production
ng build --configuration production

# Run with smaller bundle
ng serve --poll 2000 --host 0.0.0.0
```

### Backend
```bash
# Skip tests for faster builds (dev only!)
mvn clean install -DskipTests

# Run specific service only (faster)
mvn -f loan-service/pom.xml spring-boot:run
```

---

## One-Command Startup (Optional)

**For Windows PowerShell:**
```powershell
# If you have start-all-services.ps1
.\start-all-services.ps1
```

**For Linux/Mac:**
```bash
# Run in background
mvn -f eureka-server spring-boot:run > /tmp/eureka.log 2>&1 &
mvn -f api-gateway spring-boot:run > /tmp/gateway.log 2>&1 &
mvn -f auth-service spring-boot:run > /tmp/auth.log 2>&1 &
# ... etc

# Start frontend
cd frontend && npm start
```

---

## Environment Summary

**Development:**
```
Frontend: http://localhost:4200
Backend Gateway: http://localhost:8080/api
Database: In-memory (H2)
Authentication: JWT (HS256)
CORS Origins: http://localhost:4200, http://localhost:3000
```

**All configured and working!** 🚀

---

**Status: ✅ READY TO USE**

Your application should now work seamlessly without CORS or 500 errors!

If you have issues, check the troubleshooting guides or run the diagnostic commands above.

Good luck! 🎉
