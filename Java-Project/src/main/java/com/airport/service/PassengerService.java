package com.airport.service;

import com.airport.model.Booking;
import com.airport.model.Flight;
import com.airport.model.Passenger;
import com.airport.repository.BookingRepository;
import com.airport.repository.FlightRepository;
import com.airport.repository.PassengerRepository;

import java.util.*;

public class PassengerService {
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;

    public PassengerService(PassengerRepository passengerRepository, BookingRepository bookingRepository, FlightRepository flightRepository) {
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
    }

    public List<Passenger> getAllPassengers() { return passengerRepository.findAll(); }
    public Optional<Passenger> getPassengerById(String id) { return passengerRepository.findById(id); }
    public List<Passenger> searchPassengers(String q) { return passengerRepository.search(q); }

    public void createPassenger(Passenger passenger) { passengerRepository.save(passenger); }

    public void updatePassenger(Passenger passenger) { passengerRepository.update(passenger); }

    public void deletePassenger(String passengerId) { passengerRepository.deleteById(passengerId); }

    public List<PassengerFlightInfo> getPassengerFlights(String passengerId) {
        List<Booking> bookings = bookingRepository.findByPassengerId(passengerId);
        List<PassengerFlightInfo> result = new ArrayList<>();
        for (Booking b : bookings) {
            Optional<Flight> f = flightRepository.findById(b.getFlightId());
            f.ifPresent(flight -> result.add(new PassengerFlightInfo(b, flight)));
        }
        result.sort(Comparator.comparing(i -> i.getFlight().getDepartureTime()));
        return result;
    }

    public static class PassengerFlightInfo {
        private final Booking booking;
        private final Flight flight;
        public PassengerFlightInfo(Booking booking, Flight flight) { this.booking = booking; this.flight = flight; }
        public Booking getBooking() { return booking; }
        public Flight getFlight() { return flight; }
    }
}
