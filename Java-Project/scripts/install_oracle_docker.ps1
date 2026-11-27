# Installs a real Oracle Database locally (23ai/26ai Free) via Docker + Oracle SQL Developer GUI.
# MUST be run from an ELEVATED PowerShell (right-click PowerShell -> Run as administrator).
# 1) Open PowerShell as Administrator
# 2) cd C:\Users\LENOVO\Desktop\Java-Project
# 3) powershell -ExecutionPolicy Bypass -File .\scripts\install_oracle_docker.ps1
# A reboot may be required after WSL2 installation; if so, reboot and run the script again.

#requires -RunAsAdministrator
$ErrorActionPreference = 'Stop'

Write-Host '=== 1. Docker Desktop ==='
winget install --id Docker.DockerDesktop -e --accept-source-agreements --accept-package-agreements

Write-Host '=== 2. WSL2 ==='
wsl --status 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host 'Installing WSL2...'
    wsl --install --no-distribution
    Write-Warning 'WSL2 installed. REBOOT your PC, then run this script again to continue.'
    exit 0
}

Write-Host '=== 3. Start Docker ==='
$dockerExe = Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'
if (Test-Path $dockerExe) { Start-Process $dockerExe }
for ($i = 0; $i -lt 60; $i++) {
    docker info 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 5
}
if ($LASTEXITCODE -ne 0) { throw 'Docker daemon did not start. Start Docker Desktop manually and re-run.' }

Write-Host '=== 4. Pull Oracle Free (lite) image ==='
docker pull container-registry.oracle.com/database/free:latest-lite

Write-Host '=== 5. Start Oracle container on port 1521 ==='
docker rm -f oracle-free 2>&1 | Out-Null
docker run -d --name oracle-free -p 1521:1521 `
    -e ORACLE_PWD=Skyport12345 `
    container-registry.oracle.com/database/free:latest-lite

Write-Host '=== 6. Wait for database (5-15 minutes on first run) ==='
for ($i = 0; $i -lt 120; $i++) {
    $logs = docker logs oracle-free 2>&1 | Out-String
    if ($logs -match 'DATABASE IS READY TO USE') { break }
    Start-Sleep -Seconds 10
}
if ($logs -notmatch 'DATABASE IS READY TO USE') {
    throw "Oracle did not become ready. Check: docker logs oracle-free"
}

Write-Host '=== 7. Create app user skyport in FREEPDB1 ==='
$sql = @"
ALTER SESSION SET CONTAINER=FREEPDB1;
CREATE USER skyport IDENTIFIED BY skyport123;
GRANT CREATE SESSION, CREATE TABLE TO skyport;
GRANT UNLIMITED TABLESPACE TO skyport;
EXIT;
"@
$sql | docker exec -i oracle-free sqlplus -s / as sysdba

Write-Host '=== 8. Install Oracle SQL Developer (visual table browser) ==='
winget install --id Oracle.SQLDeveloper -e --accept-source-agreements --accept-package-agreements

Write-Host ''
Write-Host 'DONE. Next steps:'
Write-Host '  1. Restart the Java app: mvn javafx:run  (it will detect Oracle and use it automatically)'
Write-Host '  2. Open SQL Developer. New Connection:'
Write-Host '       Username: skyport   Password: skyport123'
Write-Host '       Host: localhost    Port: 1521    Service name: FREEPDB1'
Write-Host '  3. Expand Tables to see PASSENGERS / FLIGHTS / BOOKINGS.'
