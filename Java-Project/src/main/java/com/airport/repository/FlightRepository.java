package com.airport.repository;

import com.airport.model.Flight;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {
    List<Flight> findAll();
    Optional<Flight> findById(String flightId);
    void save(Flight flight);
    void update(Flight flight);
    void deleteById(String flightId);
}
