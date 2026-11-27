# Switch back to app mode.
# 1) Stops H2 server (releases DB lock).
# 2) Restarts the SkyPort JavaFX app.
#
# Run:
#   powershell -ExecutionPolicy Bypass -File "C:\Users\LENOVO\Desktop\Java-Project/scripts/app_mode.ps1"

$ErrorActionPreference = 'Continue'

Write-Host "=== Stopping H2 server ==="
Get-CimInstance Win32_Process -Filter "name='java.exe'" |
    Where-Object { $_.CommandLine -match 'org.h2.tools.Server' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -ErrorAction SilentlyContinue; "stopped H2 server $($_.ProcessId)" }

Start-Sleep -Seconds 3

Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.lock.db" -ErrorAction SilentlyContinue
Remove-Item "C:\Users\LENOVO\Desktop\Java-Project\data\airport.trace.db" -ErrorAction SilentlyContinue

Write-Host "=== Starting SkyPort app ==="
Start-Process powershell -ArgumentList "-NoProfile","-Command","& ""$env:USERPROFILE\.tools\apache-maven-3.9.9\bin\mvn.cmd"" javafx:run" -WindowStyle Normal -WorkingDirectory "C:\Users\LENOVO\Desktop\Java-Project"

Write-Host "App starting. Look for the SkyPort | Airport Management window."
