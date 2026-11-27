package com.airport.service;

import com.airport.model.Booking;
import com.airport.model.Flight;
import com.airport.model.Passenger;
import com.airport.repository.BookingRepository;
import com.airport.repository.FlightRepository;
import com.airport.repository.PassengerRepository;

import java.util.List;

public class BookingService {
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;

    public BookingService(BookingRepository bookingRepository,
                          PassengerRepository passengerRepository,
                          FlightRepository flightRepository) {
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.flightRepository = flightRepository;
    }

    public List<Booking> getAllBookings() { return bookingRepository.findAll(); }
    public List<Booking> getByPassenger(String passengerId) { return bookingRepository.findByPassengerId(passengerId); }
    public List<Booking> getByFlight(String flightId) { return bookingRepository.findByFlightId(flightId); }
    public List<Passenger> getAllPassengers() { return passengerRepository.findAll(); }
    public List<Flight> getAllFlights() { return flightRepository.findAll(); }

    public void createBooking(Booking booking) { bookingRepository.save(booking); }
    public void updateBooking(Booking booking) { bookingRepository.update(booking); }
    public void deleteBooking(String bookingId) { bookingRepository.deleteById(bookingId); }
}
