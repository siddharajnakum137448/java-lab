package com.airport.repository;

import com.airport.model.Booking;
import java.util.List;

public interface BookingRepository {
    List<Booking> findAll();
    List<Booking> findByPassengerId(String passengerId);
    List<Booking> findByFlightId(String flightId);
    void save(Booking booking);
    void update(Booking booking);
    void deleteById(String bookingId);
}
