package com.airport.service;

import com.airport.model.Flight;
import com.airport.model.FlightStatus;
import com.airport.repository.FlightRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlightService {
    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll().stream()
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());
    }

    public Optional<Flight> getFlightById(String id) {
        return flightRepository.findById(id);
    }

    public List<Flight> getUpcomingFlights() {
        LocalDateTime now = LocalDateTime.now();
        return flightRepository.findAll().stream()
                .filter(f -> f.getDepartureTime().isAfter(now) && f.getStatus() != FlightStatus.CANCELLED)
                .sorted(Comparator.comparing(Flight::getBoardingTime))
                .collect(Collectors.toList());
    }

    public List<Flight> getBoardingNowFlights() {
        return flightRepository.findAll().stream()
                .filter(f -> f.getStatus() == FlightStatus.BOARDING || f.getStatus() == FlightStatus.CHECK_IN_OPEN)
                .sorted(Comparator.comparing(Flight::getBoardingTime))
                .collect(Collectors.toList());
    }

    public void createFlight(Flight flight) { flightRepository.save(flight); }

    public void updateFlight(Flight flight) { flightRepository.update(flight); }

    public void deleteFlight(String flightId) { flightRepository.deleteById(flightId); }
}
