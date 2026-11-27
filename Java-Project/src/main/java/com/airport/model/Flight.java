package com.airport.model;

import java.time.LocalDateTime;

public class Flight {
    private final String flightId;
    private final String flightNumber;
    private final String airline;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
    private final LocalDateTime boardingTime;
    private final String gate;
    private final String terminal;
    private final FlightStatus status;

    public Flight(String flightId, String flightNumber, String airline, String origin, String destination,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, LocalDateTime boardingTime,
                  String gate, String terminal, FlightStatus status) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.boardingTime = boardingTime;
        this.gate = gate;
        this.terminal = terminal;
        this.status = status;
    }

    public String getFlightId() { return flightId; }
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public LocalDateTime getBoardingTime() { return boardingTime; }
    public String getGate() { return gate; }
    public String getTerminal() { return terminal; }
    public FlightStatus getStatus() { return status; }

    public String getRoute() { return origin + " → " + destination; }
}
