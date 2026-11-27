package com.airport.data;

import com.airport.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SeedData {
    private final List<Passenger> passengers;
    private final List<Flight> flights;
    private final List<Booking> bookings;

    public SeedData() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        passengers = List.of(
                new Passenger("P001", "Aarav Sharma", LocalDate.of(1995, 4, 12), "Male", "+91-9876543210", "aarav@example.com", "INP12345"),
                new Passenger("P002", "Mia Chen", LocalDate.of(1990, 9, 3), "Female", "+1-415-555-0102", "mia.chen@example.com", "USX88331"),
                new Passenger("P003", "Rahul Verma", LocalDate.of(1988, 1, 25), "Male", "+91-9988776655", "rahul.v@example.com", "INP67890"),
                new Passenger("P004", "Sara Khan", LocalDate.of(1998, 7, 19), "Female", "+44-7700-900123", "sara.khan@example.com", "UKP44556"),
                new Passenger("P005", "Daniel Park", LocalDate.of(1992, 11, 8), "Male", "+82-10-1234-5678", "dan.park@example.com", "KRP22110")
        );

        flights = List.of(
                new Flight("F001", "AI-202", "Air India", "DEL", "BOM", now.plusMinutes(75), now.plusHours(3), now.plusMinutes(30), "A12", "T3", FlightStatus.BOARDING),
                new Flight("F002", "6E-511", "IndiGo", "DEL", "BLR", now.plusHours(2), now.plusHours(4).plusMinutes(30), now.plusHours(1).plusMinutes(15), "B4", "T1", FlightStatus.CHECK_IN_OPEN),
                new Flight("F003", "EK-505", "Emirates", "DEL", "DXB", now.plusHours(5), now.plusHours(9), now.plusHours(4).plusMinutes(10), "C1", "T3", FlightStatus.SCHEDULED),
                new Flight("F004", "UK-811", "Vistara", "DEL", "GOI", now.plusDays(1).withHour(9).withMinute(20), now.plusDays(1).withHour(11).withMinute(50), now.plusDays(1).withHour(8).withMinute(35), "D7", "T3", FlightStatus.SCHEDULED),
                new Flight("F005", "SG-101", "SpiceJet", "DEL", "CCU", now.minusHours(1), now.plusHours(1), now.minusHours(1).minusMinutes(45), "A3", "T1", FlightStatus.DEPARTED),
                new Flight("F006", "BA-256", "British Airways", "DEL", "LHR", now.plusHours(3), now.plusHours(12), now.plusHours(2), "E2", "T3", FlightStatus.DELAYED)
        );

        bookings = List.of(
                new Booking("PNR1001", "P001", "F001", "12A", "Economy", BookingStatus.BOARDED, "CONFIRMED"),
                new Booking("PNR1002", "P002", "F001", "14C", "Business", BookingStatus.CHECKED_IN, "CONFIRMED"),
                new Booking("PNR1003", "P001", "F003", "22B", "Economy", BookingStatus.CONFIRMED, "CONFIRMED"),
                new Booking("PNR1004", "P003", "F002", "3D", "Economy", BookingStatus.CHECKED_IN, "CONFIRMED"),
                new Booking("PNR1005", "P004", "F004", "1A", "Business", BookingStatus.CONFIRMED, "CONFIRMED"),
                new Booking("PNR1006", "P005", "F006", "18F", "Economy", BookingStatus.CONFIRMED, "CONFIRMED"),
                new Booking("PNR1007", "P002", "F006", "7B", "Premium Economy", BookingStatus.CHECKED_IN, "CONFIRMED"),
                new Booking("PNR1008", "P003", "F005", "9C", "Economy", BookingStatus.BOARDED, "CONFIRMED")
        );
    }

    public List<Passenger> getPassengers() { return passengers; }
    public List<Flight> getFlights() { return flights; }
    public List<Booking> getBookings() { return bookings; }
}
