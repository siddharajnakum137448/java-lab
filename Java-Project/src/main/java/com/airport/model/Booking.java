package com.airport.model;

public class Booking {
    private final String bookingId;
    private final String passengerId;
    private final String flightId;
    private final String seatNumber;
    private final String travelClass;
    private final BookingStatus checkInStatus;
    private final String bookingStatus;

    public Booking(String bookingId, String passengerId, String flightId, String seatNumber,
                   String travelClass, BookingStatus checkInStatus, String bookingStatus) {
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.flightId = flightId;
        this.seatNumber = seatNumber;
        this.travelClass = travelClass;
        this.checkInStatus = checkInStatus;
        this.bookingStatus = bookingStatus;
    }

    public String getBookingId() { return bookingId; }
    public String getPassengerId() { return passengerId; }
    public String getFlightId() { return flightId; }
    public String getSeatNumber() { return seatNumber; }
    public String getTravelClass() { return travelClass; }
    public BookingStatus getCheckInStatus() { return checkInStatus; }
    public String getBookingStatus() { return bookingStatus; }
}
