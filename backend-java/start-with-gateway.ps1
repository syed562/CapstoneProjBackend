# Start API Gateway and All Microservices

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Starting Loan Management System   " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""


# Start Eureka Server
Write-Host "[2/9] Starting Eureka Server (8761)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\eureka-server'; java -jar target\eureka-server-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 5

# Start Auth Service
Write-Host "[3/9] Starting Auth Service (8083)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\auth-service'; java -jar target\auth-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start Profile Service
Write-Host "[4/9] Starting Profile Service (8082)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\profile-service'; java -jar target\profile-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start Loan Application Service
Write-Host "[5/9] Starting Loan Application Service (8084)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\loan-application-service'; java -jar target\loan-application-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start Loan Service
Write-Host "[6/9] Starting Loan Service (8085)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\loan-service'; java -jar target\loan-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start Report Service
Write-Host "[7/9] Starting Report Service (8087)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\report-service'; java -jar target\report-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start Notification Service
Write-Host "[8/9] Starting Notification Service (8088)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\notification-service'; java -jar target\notification-service-0.0.1-SNAPSHOT.jar"
Start-Sleep -Seconds 3

# Start API Gateway
Write-Host "[9/9] Starting API Gateway (8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\api-gateway'; java -jar target\api-gateway-0.0.1-SNAPSHOT.jar"

Write-Host ""
Write-Host "=====================================" -ForegroundColor Green
Write-Host "  All Services Started Successfully! " -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green
Write-Host ""
Write-Host "API Gateway URL: http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service Ports:" -ForegroundColor White
Write-Host "  - Eureka Server:         8761" -ForegroundColor Gray
Write-Host "  - Config Server:         8888" -ForegroundColor Gray
Write-Host "  - API Gateway:           8080" -ForegroundColor Gray
Write-Host "  - Auth Service:          8083" -ForegroundColor Gray
Write-Host "  - Profile Service:       8082" -ForegroundColor Gray
Write-Host "  - Loan Application:      8084" -ForegroundColor Gray
Write-Host "  - Loan Service:          8085" -ForegroundColor Gray
Write-Host "  - Report Service:        8087" -ForegroundColor Gray
Write-Host "  - Notification Service:  8088" -ForegroundColor Gray
Write-Host ""
Write-Host "Gateway Health: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "Gateway Routes: http://localhost:8080/actuator/gateway/routes" -ForegroundColor Cyan
