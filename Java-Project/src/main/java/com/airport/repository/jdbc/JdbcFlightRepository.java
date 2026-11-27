package com.airport.repository.jdbc;

import com.airport.db.Database;
import com.airport.model.Flight;
import com.airport.model.FlightStatus;
import com.airport.repository.FlightRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFlightRepository implements FlightRepository {

    @Override
    public List<Flight> findAll() {
        return query("SELECT * FROM FLIGHTS ORDER BY DEPARTURE_TIME");
    }

    @Override
    public Optional<Flight> findById(String flightId) {
        return query("SELECT * FROM FLIGHTS WHERE UPPER(FLIGHT_ID) = UPPER(?)", flightId).stream().findFirst();
    }

    @Override
    public void save(Flight f) {
        String sql = "INSERT INTO FLIGHTS (FLIGHT_ID, FLIGHT_NUMBER, AIRLINE, ORIGIN, DESTINATION, " +
                "DEPARTURE_TIME, ARRIVAL_TIME, BOARDING_TIME, GATE, TERMINAL, STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, f, true);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create flight: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Flight f) {
        String sql = "UPDATE FLIGHTS SET FLIGHT_NUMBER = ?, AIRLINE = ?, ORIGIN = ?, DESTINATION = ?, " +
                "DEPARTURE_TIME = ?, ARRIVAL_TIME = ?, BOARDING_TIME = ?, GATE = ?, TERMINAL = ?, STATUS = ? " +
                "WHERE FLIGHT_ID = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, f, false);
            ps.setString(11, f.getFlightId());
            if (ps.executeUpdate() == 0) throw new SQLException("Flight not found: " + f.getFlightId());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update flight: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(String flightId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM FLIGHTS WHERE FLIGHT_ID = ?")) {
            ps.setString(1, flightId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete flight: " + e.getMessage(), e);
        }
    }

    private void bind(PreparedStatement ps, Flight f, boolean includeId) throws SQLException {
        int i = 1;
        if (includeId) ps.setString(i++, f.getFlightId());
        ps.setString(i++, f.getFlightNumber());
        ps.setString(i++, f.getAirline());
        ps.setString(i++, f.getOrigin());
        ps.setString(i++, f.getDestination());
        ps.setTimestamp(i++, f.getDepartureTime() == null ? null : Timestamp.valueOf(f.getDepartureTime()));
        ps.setTimestamp(i++, f.getArrivalTime() == null ? null : Timestamp.valueOf(f.getArrivalTime()));
        ps.setTimestamp(i++, f.getBoardingTime() == null ? null : Timestamp.valueOf(f.getBoardingTime()));
        ps.setString(i++, f.getGate());
        ps.setString(i++, f.getTerminal());
        ps.setString(i, f.getStatus() == null ? null : f.getStatus().name());
    }

    private List<Flight> query(String sql, Object... params) {
        List<Flight> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, (String) params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("STATUS");
                    result.add(new Flight(
                            rs.getString("FLIGHT_ID"),
                            rs.getString("FLIGHT_NUMBER"),
                            rs.getString("AIRLINE"),
                            rs.getString("ORIGIN"),
                            rs.getString("DESTINATION"),
                            JdbcUtil.timestamp(rs, "DEPARTURE_TIME"),
                            JdbcUtil.timestamp(rs, "ARRIVAL_TIME"),
                            JdbcUtil.timestamp(rs, "BOARDING_TIME"),
                            rs.getString("GATE"),
                            rs.getString("TERMINAL"),
                            status == null ? null : FlightStatus.valueOf(status)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read flights: " + e.getMessage(), e);
        }
        return result;
    }
}
