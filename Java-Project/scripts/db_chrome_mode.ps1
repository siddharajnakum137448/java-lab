# Switch to Chrome DB access mode.
# 1) Closes the SkyPort JavaFX app.
# 2) Starts H2 TCP + Web server so Chrome can browse the database.
#
# Run:
#   powershell -ExecutionPolicy Bypass -File "C:\Users\LENOVO\Desktop\Java-Project/scripts/db_chrome_mode.ps1"
#
# Then open Chrome: http://localhost:8182
#   JDBC URL: jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle
#   User: sa   Password: (blank)

$ErrorActionPreference = 'Continue'

Write-Host "=== Stopping SkyPort app ==="
Get-CimInstance Win32_Process -Filter "name='java.exe'" |
    Where-Object { $_.CommandLine -match 'AirportApp|javafx:run|com.airport.ui' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -ErrorAction SilentlyContinue; "stopped $($_.ProcessId)" }

Start-Sleep -Seconds 3

Write-Host "=== Removing stale lock files ==="
Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.lock.db" -ErrorAction SilentlyContinue
Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.trace.db" -ErrorAction SilentlyContinue

Write-Host "=== Starting H2 TCP + Web server ==="
$jar = "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar"
Start-Process cmd -ArgumentList "/c","java","-cp","$jar","org.h2.tools.Server","-web","-webAllowOthers","-webPort","8182","-tcp","-tcpAllowOthers","-tcpPort","9192" -WindowStyle Normal

Start-Sleep -Seconds 8

$port8182 = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq 8182 }
$port9192 = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $_.LocalPort -eq 9192 }

if ($port8182 -and $port9192) {
    Write-Host "`n=== H2 Console is ready ===" -ForegroundColor Green
    Write-Host "Open Chrome: http://localhost:8182"
    Write-Host "JDBC URL   : jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle"
    Write-Host "User       : sa"
    Write-Host "Password   : (blank)"
    Write-Host "`nTo return to the app, run: .\scripts\app_mode.ps1"
} else {
    Write-Warning "H2 web server did not start. Check the cmd window."
}
