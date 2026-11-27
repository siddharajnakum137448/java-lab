# Airport Management System (Java + Oracle)

A modern JavaFX desktop application for airport management with full **CRUD** backed by a real database.
Primary target: **Oracle Database** (21c XE / 23ai / 26ai Free). Falls back to a local **H2 database in Oracle compatibility mode** when Oracle isn't reachable — so the app always runs.

## Features
- **Login** (demo): `admin` / `admin123`
- **Dashboard:** upcoming flights, boarding/check-in counts, delayed flights, passenger count, next departures
- **Flights:** list/filter, details, and full CRUD (create / update / delete)
- **Passengers:** search, details with bookings, and full CRUD
- **Bookings:** PNR table, passenger/flight linkage, and full CRUD
- Every table reloads from the database on navigation/refresh — changes are persisted immediately.

## Project structure
- `com.airport.model` — entities/enums (`Passenger`, `Flight`, `Booking`, statuses)
- `com.airport.repository` — repository interfaces + in-memory and JDBC implementations
- `com.airport.repository.jdbc` — Oracle/H2 JDBC repositories (ANSI SQL, prepared statements)
- `com.airport.db.Database` — connection setup, schema creation, one-time seed
- `com.airport.service` — business logic + CRUD services
- `com.airport.ui` — JavaFX application, modern CRUD forms, screens
- `src/main/resources/com/airport/ui/app.css` — modern UI stylesheet
- `src/main/resources/com/airport/db.properties` — database connection config
- `scripts/` — setup & helper scripts

## Prerequisites
- **JDK 21**
- **Maven 3.9+**

## Database configuration
Edit `src/main/resources/com/airport/db.properties`:

```properties
db.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
db.user=skyport
db.password=skyport123
```

Common service names: `FREEPDB1` (Oracle 23ai/26ai Free), `XEPDB1` (Oracle 21c XE).
Override without editing files using `-Ddb.url=... -Ddb.user=... -Ddb.password=...` or env vars
`AIRPORT_DB_URL`, `AIRPORT_DB_USER`, `AIRPORT_DB_PASSWORD`.

### Creating the Oracle app user
After installing Oracle, connect as SYS to the PDB and run:

```powershell
sql sys@localhost:1521/FREEPDB1 as sysdba
SQL> @C:\Users\LENOVO\Desktop\Java-Project\scripts\setup_oracle_user.sql
```

The app creates `PASSENGERS`, `FLIGHTS`, `BOOKINGS`, `APP_META` tables and seeds demo rows on first launch.
Deleting a passenger/flight cascades to their bookings (`ON DELETE CASCADE`).

## Run the app
From the project root (Maven at `%USERPROFILE%\.tools\apache-maven-3.9.9`):

```powershell
& "$env:USERPROFILE\.tools\apache-maven-3.9.9\bin\mvn.cmd" clean javafx:run
```

Login: `admin` / `admin123`

## View database in Chrome (H2 visual console)

Both the app and Chrome can run **at the same time** — H2 serves both via TCP, so they stay synced:

**Start both together (one command):**
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\LENOVO\Desktop\Java-Project\scripts\start_all.ps1"
```

Then open **Chrome → http://localhost:8182**
- JDBC URL: `jdbc:h2:tcp://localhost:9192/./data/airport;MODE=Oracle`
- User: `sa` | Password: *(blank)*
- Tables: `PASSENGERS`, `FLIGHTS`, `BOOKINGS`, `APP_META`

Any CRUD change you make in the app appears instantly in Chrome, and vice versa.

**Or start them separately:**
- App only: `mvn javafx:run` (or use `scripts/app_mode.ps1`)
- Chrome DB browser only: `scripts/db_chrome_mode.ps1` (closes app first, starts H2 server)

## Install real Oracle (visual tables via SQL Developer)
Oracle's native Windows installer rejects Windows 11 Home. The supported route is Oracle Free in Docker:

```powershell
# Run PowerShell as Administrator
cd C:\Users\LENOVO\Desktop\Java-Project
powershell -ExecutionPolicy Bypass -File .\scripts\install_oracle_docker.ps1
```

This installs Docker Desktop, WSL2, Oracle Free (lite image) on port `1521`, creates the `skyport` user, and installs Oracle SQL Developer.
Then in SQL Developer: `skyport` / `skyport123`, host `localhost`, port `1521`, service `FREEPDB1`.

## Notes
- Seed flight times are generated relative to startup so "upcoming" and "boarding" views are meaningful.
- Demo credentials are static — do not use as-is in production.
