package com.airport.repository.jdbc;

import com.airport.db.Database;
import com.airport.model.Booking;
import com.airport.model.BookingStatus;
import com.airport.repository.BookingRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookingRepository implements BookingRepository {

    @Override
    public List<Booking> findAll() {
        return query("SELECT * FROM BOOKINGS ORDER BY BOOKING_ID", null);
    }

    @Override
    public List<Booking> findByPassengerId(String passengerId) {
        return query("SELECT * FROM BOOKINGS WHERE UPPER(PASSENGER_ID) = UPPER(?) ORDER BY BOOKING_ID", passengerId);
    }

    @Override
    public List<Booking> findByFlightId(String flightId) {
        return query("SELECT * FROM BOOKINGS WHERE UPPER(FLIGHT_ID) = UPPER(?) ORDER BY BOOKING_ID", flightId);
    }

    @Override
    public void save(Booking b) {
        String sql = "INSERT INTO BOOKINGS (BOOKING_ID, PASSENGER_ID, FLIGHT_ID, SEAT_NUMBER, TRAVEL_CLASS, CHECK_IN_STATUS, BOOKING_STATUS) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, b);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create booking: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Booking b) {
        String sql = "UPDATE BOOKINGS SET PASSENGER_ID = ?, FLIGHT_ID = ?, SEAT_NUMBER = ?, TRAVEL_CLASS = ?, CHECK_IN_STATUS = ?, BOOKING_STATUS = ? " +
                "WHERE BOOKING_ID = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, b);
            ps.setString(7, b.getBookingId());
            if (ps.executeUpdate() == 0) throw new SQLException("Booking not found: " + b.getBookingId());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update booking: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(String bookingId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM BOOKINGS WHERE BOOKING_ID = ?")) {
            ps.setString(1, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete booking: " + e.getMessage(), e);
        }
    }

    private void bind(PreparedStatement ps, Booking b) throws SQLException {
        ps.setString(1, b.getBookingId());
        ps.setString(2, b.getPassengerId());
        ps.setString(3, b.getFlightId());
        ps.setString(4, b.getSeatNumber());
        ps.setString(5, b.getTravelClass());
        ps.setString(6, b.getCheckInStatus() == null ? null : b.getCheckInStatus().name());
        ps.setString(7, b.getBookingStatus());
    }

    private List<Booking> query(String sql, String param) {
        List<Booking> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String checkIn = rs.getString("CHECK_IN_STATUS");
                    result.add(new Booking(
                            rs.getString("BOOKING_ID"),
                            rs.getString("PASSENGER_ID"),
                            rs.getString("FLIGHT_ID"),
                            rs.getString("SEAT_NUMBER"),
                            rs.getString("TRAVEL_CLASS"),
                            checkIn == null ? null : BookingStatus.valueOf(checkIn),
                            rs.getString("BOOKING_STATUS")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read bookings: " + e.getMessage(), e);
        }
        return result;
    }
}
