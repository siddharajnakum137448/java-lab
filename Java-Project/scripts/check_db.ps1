# Inspect the local database the app is currently using.
# Run from the project root:  powershell -ExecutionPolicy Bypass -File .\scripts\check_db.ps1
# Optional: -Table FLIGHTS   (or PASSENGERS / BOOKINGS / APP_META)

param(
    [ValidateSet('SUMMARY', 'PASSENGERS', 'FLIGHTS', 'BOOKINGS', 'APP_META')]
    [string]$Table = 'SUMMARY'
)

$ErrorActionPreference = 'Stop'

# Make the DB path relative to the project root, not the current directory.
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

$jar = Join-Path $env:USERPROFILE '.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar'
if (-not (Test-Path $jar)) { throw "H2 jar not found at $jar" }

$url = 'jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle'

$sql = switch ($Table) {
    'PASSENGERS' { 'SELECT * FROM PASSENGERS ORDER BY PASSENGER_ID;' }
    'FLIGHTS'    { 'SELECT FLIGHT_ID, FLIGHT_NUMBER, AIRLINE, ORIGIN, DESTINATION, DEPARTURE_TIME, BOARDING_TIME, GATE, TERMINAL, STATUS FROM FLIGHTS ORDER BY DEPARTURE_TIME;' }
    'BOOKINGS'   { 'SELECT * FROM BOOKINGS ORDER BY BOOKING_ID;' }
    'APP_META'   { 'SELECT * FROM APP_META;' }
    default {
        "SELECT 'PASSENGERS' AS TABLE_NAME, COUNT(*) AS ROWS FROM PASSENGERS;" +
        "SELECT 'FLIGHTS' AS TABLE_NAME, COUNT(*) AS ROWS FROM FLIGHTS;" +
        "SELECT 'BOOKINGS' AS TABLE_NAME, COUNT(*) AS ROWS FROM BOOKINGS;"
    }
}

Write-Host "Database: H2 file 'data/airport.mv.db' (Oracle compatibility mode)"
Write-Host "JDBC URL : $url"
Write-Host "User     : sa   Password: (empty)"
Write-Host "NOTE: Close the JavaFX app before running this (H2 uses an exclusive file lock)."
Write-Host ''
& java -cp $jar org.h2.tools.Shell -url $url -user sa -sql $sql
