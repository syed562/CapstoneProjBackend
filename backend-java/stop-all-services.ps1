# Stop All Microservices
# This script stops all Java processes running the microservices

Write-Host "Stopping all microservices..." -ForegroundColor Red

# Get all Java processes running the services
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Found $($javaProcesses.Count) Java process(es) running" -ForegroundColor Yellow
    
    foreach ($process in $javaProcesses) {
        Write-Host "Stopping process $($process.Id)..." -ForegroundColor Cyan
        Stop-Process -Id $process.Id -Force
    }
    
    Write-Host "`nAll services stopped successfully!" -ForegroundColor Green
} else {
    Write-Host "No Java processes found running." -ForegroundColor Yellow
}
