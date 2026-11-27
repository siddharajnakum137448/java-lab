package com.airport.model;

import java.time.LocalDate;

public class Passenger {
    private final String passengerId;
    private final String fullName;
    private final LocalDate dob;
    private final String gender;
    private final String phone;
    private final String email;
    private final String passportOrId;

    public Passenger(String passengerId, String fullName, LocalDate dob, String gender,
                     String phone, String email, String passportOrId) {
        this.passengerId = passengerId;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.passportOrId = passportOrId;
    }

    public String getPassengerId() { return passengerId; }
    public String getFullName() { return fullName; }
    public LocalDate getDob() { return dob; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassportOrId() { return passportOrId; }

    @Override
    public String toString() {
        return fullName + " (" + passengerId + ")";
    }
}
