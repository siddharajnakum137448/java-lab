package com.airport.db;

import com.airport.data.SeedData;
import com.airport.repository.BookingRepository;
import com.airport.repository.FlightRepository;
import com.airport.repository.PassengerRepository;
import com.airport.repository.jdbc.JdbcBookingRepository;
import com.airport.repository.jdbc.JdbcFlightRepository;
import com.airport.repository.jdbc.JdbcPassengerRepository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Central JDBC configuration. Connects to Oracle first; if no Oracle instance is reachable
 * it falls back to a local H2 database running in Oracle compatibility mode so the CRUD app
 * remains fully usable. All repositories use plain ANSI SQL that works on both engines.
 */
public final class Database {
    private static boolean initialized;
    private static String driver;
    private static String url;
    private static String user;
    private static String password;
    private static String mode = "not initialized";
    private static String lastError = "";

    private Database() { }

    public static synchronized String getMode() {
        init();
        return mode;
    }

    public static synchronized String getLastError() {
        return lastError;
    }

    public static Connection getConnection() throws SQLException {
        init();
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found: " + driver, e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static synchronized void init() {
        if (initialized) return;
        initialized = true; // re-entrant: repositories call getConnection() while seeding

        Properties p = new Properties();
        try (InputStream in = Database.class.getResourceAsStream("/com/airport/db.properties")) {
            if (in != null) p.load(in);
        } catch (Exception e) {
            lastError = "Could not read db.properties: " + e.getMessage();
        }

        DriverManager.setLoginTimeout(3);

        String oracleDriver = override("db.driver", "AIRPORT_DB_DRIVER", p, "oracle.jdbc.OracleDriver");
        String oracleUrl = override("db.url", "AIRPORT_DB_URL", p, "jdbc:oracle:thin:@localhost:1521/FREEPDB1");
        String oracleUser = override("db.user", "AIRPORT_DB_USER", p, "skyport");
        String oraclePass = override("db.password", "AIRPORT_DB_PASSWORD", p, "skyport123");

        boolean connected = false;
        try {
            connected = tryConnect(oracleDriver, oracleUrl, oracleUser, oraclePass);
            if (connected) {
                driver = oracleDriver;
                url = oracleUrl;
                user = oracleUser;
                password = oraclePass;
                mode = "Oracle Database (" + oracleUrl + ")";
            }
        } catch (Exception e) {
            lastError = "Oracle connection failed: " + e.getMessage();
        }

        if (!connected) {
            String fbDriver = p.getProperty("db.fallback.driver", "org.h2.Driver");
            String fbUrl = p.getProperty("db.fallback.url", "jdbc:h2:file:./data/airport;MODE=Oracle");
            String fbUser = p.getProperty("db.fallback.user", "sa");
            String fbPass = p.getProperty("db.fallback.password", "");
            try {
                connected = tryConnect(fbDriver, fbUrl, fbUser, fbPass);
                if (connected) {
                    driver = fbDriver;
                    url = fbUrl;
                    user = fbUser;
                    password = fbPass;
                    mode = "H2 fallback (Oracle mode) - Oracle not reachable";
                }
            } catch (Exception e) {
                lastError += " | H2 fallback failed: " + e.getMessage();
            }
        }

        if (!connected) {
            initialized = false;
            throw new IllegalStateException("No database available. " + lastError);
        }

        try {
            createSchema();
            seedIfEmpty();
        } catch (SQLException e) {
            initialized = false;
            lastError = "Schema/seed error: " + e.getMessage();
            throw new IllegalStateException(lastError, e);
        }
    }

    private static String override(String propKey, String envKey, Properties p, String def) {
        String value = System.getProperty(propKey);
        if (value == null || value.isBlank()) value = System.getenv(envKey);
        if (value == null || value.isBlank()) value = p.getProperty(propKey, def);
        return value;
    }

    private static boolean tryConnect(String drv, String u, String usr, String pwd) throws Exception {
        Class.forName(drv);
        try (Connection c = DriverManager.getConnection(u, usr, pwd);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT 1 FROM DUAL")) {
            return rs.next();
        }
    }

    private static void createSchema() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            ensureTable(c, st, "APP_META",
                    "CREATE TABLE APP_META (KEY_NAME VARCHAR2(80) PRIMARY KEY, KEY_VALUE VARCHAR2(200))");
            ensureTable(c, st, "PASSENGERS",
                    "CREATE TABLE PASSENGERS (" +
                            "PASSENGER_ID VARCHAR2(20) PRIMARY KEY, " +
                            "FULL_NAME VARCHAR2(120) NOT NULL, " +
                            "DOB DATE, " +
                            "GENDER VARCHAR2(20), " +
                            "PHONE VARCHAR2(40), " +
                            "EMAIL VARCHAR2(140), " +
                            "PASSPORT_OR_ID VARCHAR2(60))");
            ensureTable(c, st, "FLIGHTS",
                    "CREATE TABLE FLIGHTS (" +
                            "FLIGHT_ID VARCHAR2(20) PRIMARY KEY, " +
                            "FLIGHT_NUMBER VARCHAR2(30) NOT NULL, " +
                            "AIRLINE VARCHAR2(100), " +
                            "ORIGIN VARCHAR2(10), " +
                            "DESTINATION VARCHAR2(10), " +
                            "DEPARTURE_TIME TIMESTAMP, " +
                            "ARRIVAL_TIME TIMESTAMP, " +
                            "BOARDING_TIME TIMESTAMP, " +
                            "GATE VARCHAR2(20), " +
                            "TERMINAL VARCHAR2(20), " +
                            "STATUS VARCHAR2(30))");
            ensureTable(c, st, "BOOKINGS",
                    "CREATE TABLE BOOKINGS (" +
                            "BOOKING_ID VARCHAR2(30) PRIMARY KEY, " +
                            "PASSENGER_ID VARCHAR2(20) NOT NULL, " +
                            "FLIGHT_ID VARCHAR2(20) NOT NULL, " +
                            "SEAT_NUMBER VARCHAR2(10), " +
                            "TRAVEL_CLASS VARCHAR2(30), " +
                            "CHECK_IN_STATUS VARCHAR2(30), " +
                            "BOOKING_STATUS VARCHAR2(30), " +
                            "CONSTRAINT FK_BOOKING_PASSION FOREIGN KEY (PASSENGER_ID) REFERENCES PASSENGERS(PASSENGER_ID) ON DELETE CASCADE, " +
                            "CONSTRAINT FK_BOOKING_FLIGHT FOREIGN KEY (FLIGHT_ID) REFERENCES FLIGHTS(FLIGHT_ID) ON DELETE CASCADE)");
        }
    }

    private static void ensureTable(Connection c, Statement st, String table, String ddl) throws SQLException {
        boolean exists = false;
        try (ResultSet rs = c.getMetaData().getTables(null, null, table, new String[]{"TABLE"})) {
            exists = rs.next();
        }
        if (!exists) st.execute(ddl);
    }

    private static void seedIfEmpty() throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT KEY_VALUE FROM APP_META WHERE KEY_NAME = 'SEEDED'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return;
        }

        SeedData seed = new SeedData();
        PassengerRepository passengers = new JdbcPassengerRepository();
        FlightRepository flights = new JdbcFlightRepository();
        BookingRepository bookings = new JdbcBookingRepository();

        seed.getPassengers().forEach(passengers::save);
        seed.getFlights().forEach(flights::save);
        seed.getBookings().forEach(bookings::save);

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO APP_META (KEY_NAME, KEY_VALUE) VALUES ('SEEDED', 'TRUE')")) {
            ps.executeUpdate();
        }
    }
}
