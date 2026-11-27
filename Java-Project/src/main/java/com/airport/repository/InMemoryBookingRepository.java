package com.airport.repository;

import com.airport.model.Booking;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryBookingRepository implements BookingRepository {
    private final List<Booking> bookings = new ArrayList<>();

    public InMemoryBookingRepository(List<Booking> seed) {
        if (seed != null) bookings.addAll(seed);
    }

    @Override
    public List<Booking> findAll() {
        return new ArrayList<>(bookings);
    }

    @Override
    public List<Booking> findByPassengerId(String passengerId) {
        return bookings.stream().filter(b -> b.getPassengerId().equalsIgnoreCase(passengerId)).collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByFlightId(String flightId) {
        return bookings.stream().filter(b -> b.getFlightId().equalsIgnoreCase(flightId)).collect(Collectors.toList());
    }

    @Override
    public void save(Booking booking) { bookings.add(booking); }

    @Override
    public void update(Booking booking) {
        for (int i = 0; i < bookings.size(); i++) {
            if (bookings.get(i).getBookingId().equalsIgnoreCase(booking.getBookingId())) {
                bookings.set(i, booking);
                return;
            }
        }
        throw new IllegalStateException("Booking not found: " + booking.getBookingId());
    }

    @Override
    public void deleteById(String bookingId) {
        bookings.removeIf(b -> b.getBookingId().equalsIgnoreCase(bookingId));
    }
}
