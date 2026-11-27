# Start everything together: H2 TCP/Web server + SkyPort JavaFX app.
# Both run at the same time and share the same database via TCP.
#
# Run:
#   powershell -ExecutionPolicy Bypass -File "C:\Users\LENOVO\Desktop\Java-Project/scripts/start_all.ps1"
#
# Then open Chrome: http://localhost:8182
#   JDBC URL: jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle
#   User: sa   Password: (blank)

$ErrorActionPreference = 'Continue'

Write-Host "=== Stopping any existing Java/H2 processes ==="
Get-CimInstance Win32_Process -Filter "name='java.exe'" |
    Where-Object { $_.CommandLine -match 'AirportApp|javafx:run|com.airport.ui|org.h2.tools.Server' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -ErrorAction SilentlyContinue; "stopped $($_.ProcessId)" }

Start-Sleep -Seconds 3

Write-Host "=== Cleaning lock files ==="
Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.lock.db" -ErrorAction SilentlyContinue
Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.trace.db" -ErrorAction SilentlyContinue

Write-Host "=== Starting H2 TCP + Web server ==="
$jar = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar"
Start-Process cmd -ArgumentList "/c","java","-cp","$jar","org.h2.tools.Server","-web","-webAllowOthers","-webPort","8182","-tcp","-tcpAllowOthers","-tcpPort","9192" -WindowStyle Normal

Start-Sleep -Seconds 8

$port8182 = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq 8182 }
$port9192 = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq 9192 }

if ($port8182 -and $port9192) {
    Write-Host "`n=== H2 server ready (ports 8182 web / 9192 tcp) ===" -ForegroundColor Green
} else {
    Write-Warning "H2 server may still be starting. Check the cmd window."
}

Write-Host "`n=== Starting SkyPort JavaFX app (connects via TCP to H2 on port 9192) ==="
Start-Process powershell -ArgumentList "-NoProfile","-Command","& ""$env:USERPROFILE\.tools\apache-maven-3.9.9\bin\mvn.cmd"" javafx:run" -WindowStyle Normal -WorkingDirectory "C:\Users\LENOVO\Desktop\Java-Project"

Write-Host "`n=== BOTH RUNNING ===" -ForegroundColor Green
Write-Host "App: SkyPort | Airport Management window"
Write-Host "Chrome DB: http://localhost:8182"
Write-Host "JDBC URL: jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle"
Write-Host "User: sa   Password: (blank)"
