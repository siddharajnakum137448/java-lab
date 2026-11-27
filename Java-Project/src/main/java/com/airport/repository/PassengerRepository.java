package com.airport.repository;

import com.airport.model.Passenger;
import java.util.List;
import java.util.Optional;

public interface PassengerRepository {
    List<Passenger> findAll();
    Optional<Passenger> findById(String passengerId);
    List<Passenger> search(String query);
    void save(Passenger passenger);
    void update(Passenger passenger);
    void deleteById(String passengerId);
}
