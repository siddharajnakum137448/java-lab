package com.airport.repository;

import com.airport.model.Flight;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryFlightRepository implements FlightRepository {
    private final List<Flight> flights = new ArrayList<>();

    public InMemoryFlightRepository(List<Flight> seed) {
        if (seed != null) flights.addAll(seed);
    }

    @Override
    public List<Flight> findAll() {
        return new ArrayList<>(flights);
    }

    @Override
    public Optional<Flight> findById(String flightId) {
        return flights.stream().filter(f -> f.getFlightId().equalsIgnoreCase(flightId)).findFirst();
    }

    @Override
    public void save(Flight flight) { flights.add(flight); }

    @Override
    public void update(Flight flight) {
        for (int i = 0; i < flights.size(); i++) {
            if (flights.get(i).getFlightId().equalsIgnoreCase(flight.getFlightId())) {
                flights.set(i, flight);
                return;
            }
        }
        throw new IllegalStateException("Flight not found: " + flight.getFlightId());
    }

    @Override
    public void deleteById(String flightId) {
        flights.removeIf(f -> f.getFlightId().equalsIgnoreCase(flightId));
    }
}
