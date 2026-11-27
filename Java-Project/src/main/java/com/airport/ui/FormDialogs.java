package com.airport.ui;

import com.airport.model.Booking;
import com.airport.model.BookingStatus;
import com.airport.model.Flight;
import com.airport.model.FlightStatus;
import com.airport.model.Passenger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/** Modal CRUD forms shared by the Flights, Passengers and Bookings screens. */
public final class FormDialogs {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FormDialogs() { }

    public static Optional<Flight> flight(Flight existing, Stage owner) {
        Dialog<Flight> dialog = dialog(existing == null ? "New Flight" : "Edit Flight", owner);
        GridPane grid = grid();
        int r = 0;
        TextField id = addField(grid, r++, "Flight ID", existing == null ? "F" + System.currentTimeMillis() % 100000 : existing.getFlightId());
        if (existing != null) id.setEditable(false);
        TextField number = addField(grid, r++, "Flight Number", existing == null ? "" : existing.getFlightNumber());
        TextField airline = addField(grid, r++, "Airline", existing == null ? "" : existing.getAirline());
        TextField origin = addField(grid, r++, "Origin (e.g. DEL)", existing == null ? "DEL" : existing.getOrigin());
        TextField destination = addField(grid, r++, "Destination (e.g. BOM)", existing == null ? "" : existing.getDestination());
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        TextField dep = addField(grid, r++, "Departure (yyyy-MM-dd HH:mm)", existing == null ? DTF.format(now.plusHours(2)) : DTF.format(existing.getDepartureTime()));
        TextField arr = addField(grid, r++, "Arrival (yyyy-MM-dd HH:mm)", existing == null ? DTF.format(now.plusHours(4)) : DTF.format(existing.getArrivalTime()));
        TextField board = addField(grid, r++, "Boarding (yyyy-MM-dd HH:mm)", existing == null ? DTF.format(now.plusHours(1)) : DTF.format(existing.getBoardingTime()));
        TextField gate = addField(grid, r++, "Gate", existing == null ? "" : existing.getGate());
        TextField terminal = addField(grid, r++, "Terminal", existing == null ? "" : existing.getTerminal());
        ComboBox<FlightStatus> status = addCombo(grid, r++, "Status",
                FXCollections.observableArrayList(FlightStatus.values()),
                existing == null ? FlightStatus.SCHEDULED : existing.getStatus());

        ButtonType save = saveButton(dialog);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> {
            if (b != save) return null;
            if (id.getText().isBlank() || number.getText().isBlank()) {
                warning(owner, "Flight ID and Flight Number are required."); return null;
            }
            try {
                return new Flight(id.getText().trim(), number.getText().trim(), airline.getText().trim(),
                        origin.getText().trim(), destination.getText().trim(),
                        parseDateTime(dep.getText()), parseDateTime(arr.getText()), parseDateTime(board.getText()),
                        gate.getText().trim(), terminal.getText().trim(), status.getValue());
            } catch (DateTimeParseException e) {
                warning(owner, "Date/time must use format yyyy-MM-dd HH:mm (e.g. 2026-11-19 14:30).");
                return null;
            }
        });
        return dialog.showAndWait();
    }

    public static Optional<Passenger> passenger(Passenger existing, Stage owner) {
        Dialog<Passenger> dialog = dialog(existing == null ? "New Passenger" : "Edit Passenger", owner);
        GridPane grid = grid();
        int r = 0;
        TextField id = addField(grid, r++, "Passenger ID", existing == null ? "P" + System.currentTimeMillis() % 100000 : existing.getPassengerId());
        if (existing != null) id.setEditable(false);
        TextField name = addField(grid, r++, "Full Name", existing == null ? "" : existing.getFullName());

        Label dobLabel = new Label("Date of Birth");
        dobLabel.getStyleClass().add("detail-label");
        DatePicker dob = new DatePicker(existing == null ? LocalDate.now().minusYears(30) : existing.getDob());
        grid.add(dobLabel, 0, r); grid.add(dob, 1, r++);

        TextField gender = addField(grid, r++, "Gender", existing == null ? "" : existing.getGender());
        TextField phone = addField(grid, r++, "Phone", existing == null ? "" : existing.getPhone());
        TextField email = addField(grid, r++, "Email", existing == null ? "" : existing.getEmail());
        TextField passport = addField(grid, r++, "Passport / ID", existing == null ? "" : existing.getPassportOrId());

        ButtonType save = saveButton(dialog);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> {
            if (b != save) return null;
            if (id.getText().isBlank() || name.getText().isBlank()) {
                warning(owner, "Passenger ID and Full Name are required."); return null;
            }
            return new Passenger(id.getText().trim(), name.getText().trim(), dob.getValue(),
                    gender.getText().trim(), phone.getText().trim(), email.getText().trim(), passport.getText().trim());
        });
        return dialog.showAndWait();
    }

    public static Optional<Booking> booking(Booking existing, List<Passenger> passengers,
                                            List<Flight> flights, Stage owner) {
        Dialog<Booking> dialog = dialog(existing == null ? "New Booking" : "Edit Booking", owner);
        GridPane grid = grid();
        int r = 0;
        TextField id = addField(grid, r++, "Booking (PNR) ID", existing == null ? "PNR" + System.currentTimeMillis() % 100000 : existing.getBookingId());
        if (existing != null) id.setEditable(false);

        ObservableList<String> paxItems = FXCollections.observableArrayList();
        passengers.forEach(p -> paxItems.add(p.getPassengerId() + " - " + p.getFullName()));
        ComboBox<String> pax = addCombo(grid, r++, "Passenger", paxItems,
                existing == null ? (paxItems.isEmpty() ? null : paxItems.get(0))
                        : existing.getPassengerId() + " - " + passengerName(passengers, existing.getPassengerId()));

        ObservableList<String> flightItems = FXCollections.observableArrayList();
        flights.forEach(f -> flightItems.add(f.getFlightId() + " - " + f.getFlightNumber() + " (" + f.getRoute() + ")"));
        ComboBox<String> flight = addCombo(grid, r++, "Flight", flightItems,
                existing == null ? (flightItems.isEmpty() ? null : flightItems.get(0))
                        : existing.getFlightId() + " - " + flightNumber(flights, existing.getFlightId()));

        TextField seat = addField(grid, r++, "Seat", existing == null ? "" : existing.getSeatNumber());
        TextField travelClass = addField(grid, r++, "Travel Class", existing == null ? "Economy" : existing.getTravelClass());
        ComboBox<BookingStatus> checkIn = addCombo(grid, r++, "Check-in Status",
                FXCollections.observableArrayList(BookingStatus.values()),
                existing == null ? BookingStatus.CONFIRMED : existing.getCheckInStatus());
        TextField bookingStatus = addField(grid, r++, "Booking Status", existing == null ? "CONFIRMED" : existing.getBookingStatus());

        ButtonType save = saveButton(dialog);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(b -> {
            if (b != save) return null;
            if (id.getText().isBlank() || pax.getValue() == null || flight.getValue() == null) {
                warning(owner, "Booking ID, passenger and flight are required (create passengers/flights first).");
                return null;
            }
            return new Booking(id.getText().trim(), before(pax.getValue(), " - "), before(flight.getValue(), " - "),
                    seat.getText().trim(), travelClass.getText().trim(), checkIn.getValue(), bookingStatus.getText().trim());
        });
        return dialog.showAndWait();
    }

    private static String passengerName(List<Passenger> list, String id) {
        return list.stream().filter(p -> p.getPassengerId().equalsIgnoreCase(id))
                .map(Passenger::getFullName).findFirst().orElse(id);
    }

    private static String flightNumber(List<Flight> list, String id) {
        return list.stream().filter(f -> f.getFlightId().equalsIgnoreCase(id))
                .map(Flight::getFlightNumber).findFirst().orElse(id);
    }

    private static String before(String s, String delimiter) {
        int i = s.indexOf(delimiter);
        return i < 0 ? s.trim() : s.substring(0, i).trim();
    }

    private static LocalDateTime parseDateTime(String text) {
        if (text == null || text.isBlank()) return null;
        return LocalDateTime.parse(text.trim(), DTF);
    }

    private static <T> Dialog<T> dialog(String title, Stage owner) {
        Dialog<T> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle(title);
        d.setHeaderText(null);
        return d;
    }

    private static GridPane grid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(9);
        grid.setPadding(new Insets(16));
        return grid;
    }

    private static TextField addField(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("detail-label");
        TextField tf = new TextField(value == null ? "" : value);
        tf.setPrefWidth(280);
        grid.add(l, 0, row);
        grid.add(tf, 1, row);
        return tf;
    }

    private static <T> ComboBox<T> addCombo(GridPane grid, int row, String label,
                                            ObservableList<T> items, T selected) {
        Label l = new Label(label);
        l.getStyleClass().add("detail-label");
        ComboBox<T> cb = new ComboBox<>(items);
        cb.setValue(selected);
        cb.setPrefWidth(280);
        grid.add(l, 0, row);
        grid.add(cb, 1, row);
        return cb;
    }

    private static ButtonType saveButton(Dialog<?> dialog) {
        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
        return save;
    }

    private static void warning(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
