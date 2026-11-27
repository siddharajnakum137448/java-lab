package com.airport.repository.jdbc;

import com.airport.db.Database;
import com.airport.model.Passenger;
import com.airport.repository.PassengerRepository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPassengerRepository implements PassengerRepository {

    @Override
    public List<Passenger> findAll() {
        return query("SELECT * FROM PASSENGERS ORDER BY PASSENGER_ID");
    }

    @Override
    public Optional<Passenger> findById(String passengerId) {
        List<Passenger> list = query("SELECT * FROM PASSENGERS WHERE UPPER(PASSENGER_ID) = UPPER(?)", passengerId);
        return list.stream().findFirst();
    }

    @Override
    public List<Passenger> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String like = "%" + query.toLowerCase() + "%";
        return query("SELECT * FROM PASSENGERS WHERE " +
                        "LOWER(FULL_NAME) LIKE ? OR LOWER(PASSENGER_ID) LIKE ? OR " +
                        "LOWER(PASSPORT_OR_ID) LIKE ? OR LOWER(EMAIL) LIKE ? " +
                        "ORDER BY PASSENGER_ID",
                like, like, like, like);
    }

    @Override
    public void save(Passenger p) {
        String sql = "INSERT INTO PASSENGERS (PASSENGER_ID, FULL_NAME, DOB, GENDER, PHONE, EMAIL, PASSPORT_OR_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPassengerId());
            ps.setString(2, p.getFullName());
            ps.setDate(3, p.getDob() == null ? null : Date.valueOf(p.getDob()));
            ps.setString(4, p.getGender());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getEmail());
            ps.setString(7, p.getPassportOrId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not create passenger: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Passenger p) {
        String sql = "UPDATE PASSENGERS SET FULL_NAME = ?, DOB = ?, GENDER = ?, PHONE = ?, EMAIL = ?, PASSPORT_OR_ID = ? " +
                "WHERE PASSENGER_ID = ?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getFullName());
            ps.setDate(2, p.getDob() == null ? null : Date.valueOf(p.getDob()));
            ps.setString(3, p.getGender());
            ps.setString(4, p.getPhone());
            ps.setString(5, p.getEmail());
            ps.setString(6, p.getPassportOrId());
            ps.setString(7, p.getPassengerId());
            if (ps.executeUpdate() == 0) throw new SQLException("Passenger not found: " + p.getPassengerId());
        } catch (SQLException e) {
            throw new RuntimeException("Could not update passenger: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(String passengerId) {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM PASSENGERS WHERE PASSENGER_ID = ?")) {
            ps.setString(1, passengerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete passenger: " + e.getMessage(), e);
        }
    }

    private List<Passenger> query(String sql, Object... params) {
        List<Passenger> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, (String) params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Passenger(
                            rs.getString("PASSENGER_ID"),
                            rs.getString("FULL_NAME"),
                            JdbcUtil.date(rs, "DOB"),
                            rs.getString("GENDER"),
                            rs.getString("PHONE"),
                            rs.getString("EMAIL"),
                            rs.getString("PASSPORT_OR_ID")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not read passengers: " + e.getMessage(), e);
        }
        return result;
    }
}
