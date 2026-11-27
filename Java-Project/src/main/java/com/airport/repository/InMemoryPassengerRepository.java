package com.airport.repository;

import com.airport.model.Passenger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryPassengerRepository implements PassengerRepository {
    private final List<Passenger> passengers = new ArrayList<>();

    public InMemoryPassengerRepository(List<Passenger> seed) {
        if (seed != null) passengers.addAll(seed);
    }

    @Override
    public List<Passenger> findAll() {
        return new ArrayList<>(passengers);
    }

    @Override
    public Optional<Passenger> findById(String passengerId) {
        return passengers.stream().filter(p -> p.getPassengerId().equalsIgnoreCase(passengerId)).findFirst();
    }

    @Override
    public List<Passenger> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String q = query.toLowerCase();
        return passengers.stream().filter(p ->
                p.getFullName().toLowerCase().contains(q)
                        || p.getPassengerId().toLowerCase().contains(q)
                        || p.getPassportOrId().toLowerCase().contains(q)
                        || p.getEmail().toLowerCase().contains(q)
        ).collect(Collectors.toList());
    }

    @Override
    public void save(Passenger passenger) { passengers.add(passenger); }

    @Override
    public void update(Passenger passenger) {
        for (int i = 0; i < passengers.size(); i++) {
            if (passengers.get(i).getPassengerId().equalsIgnoreCase(passenger.getPassengerId())) {
                passengers.set(i, passenger);
                return;
            }
        }
        throw new IllegalStateException("Passenger not found: " + passenger.getPassengerId());
    }

    @Override
    public void deleteById(String passengerId) {
        passengers.removeIf(p -> p.getPassengerId().equalsIgnoreCase(passengerId));
    }
}
