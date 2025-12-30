# Start All Microservices
# This script starts all 5 services in separate terminal windows

Write-Host "Starting all microservices..." -ForegroundColor Green

# Start Auth Service (Port 8083)
Write-Host "Starting Auth Service on port 8083..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\syeds\Downloads\Capstone_Backend\backend-java\auth-service'; Write-Host 'Starting Auth Service...' -ForegroundColor Yellow; java -jar target\auth-service-0.0.1-SNAPSHOT.jar"

Start-Sleep -Seconds 2

# Start Profile Service (Port 8082)
Write-Host "Starting Profile Service on port 8082..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\syeds\Downloads\Capstone_Backend\backend-java\profile-service'; Write-Host 'Starting Profile Service...' -ForegroundColor Yellow; java -jar target\profile-service-0.0.1-SNAPSHOT.jar"

Start-Sleep -Seconds 2

# Start Loan Application Service (Port 8084)
Write-Host "Starting Loan Application Service on port 8084..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\syeds\Downloads\Capstone_Backend\backend-java\loan-application-service'; Write-Host 'Starting Loan Application Service...' -ForegroundColor Yellow; java -jar target\loan-application-service-0.0.1-SNAPSHOT.jar"

Start-Sleep -Seconds 2

# Start Loan Service (Port 8085)
Write-Host "Starting Loan Service on port 8085..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\syeds\Downloads\Capstone_Backend\backend-java\loan-service'; Write-Host 'Starting Loan Service...' -ForegroundColor Yellow; java -jar target\loan-service-0.0.1-SNAPSHOT.jar"

Start-Sleep -Seconds 2

# Start Report Service (Port 8087)
Write-Host "Starting Report Service on port 8087..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd 'c:\Users\syeds\Downloads\Capstone_Backend\backend-java\report-service'; Write-Host 'Starting Report Service...' -ForegroundColor Yellow; java -jar target\report-service-0.0.1-SNAPSHOT.jar"

Write-Host "`nAll services started in separate windows!" -ForegroundColor Green
Write-Host "To stop all services, close each PowerShell window or press Ctrl+C in each window." -ForegroundColor Yellow
Write-Host "`nService URLs:" -ForegroundColor Cyan
Write-Host "  Auth Service:            http://localhost:8083"
Write-Host "  Profile Service:         http://localhost:8082"
Write-Host "  Loan Application Service: http://localhost:8084"
Write-Host "  Loan Service:            http://localhost:8085"
Write-Host "  Report Service:          http://localhost:8087"
