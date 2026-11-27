package com.airport.ui;

import com.airport.db.Database;
import com.airport.model.Booking;
import com.airport.model.Flight;
import com.airport.model.FlightStatus;
import com.airport.model.Passenger;
import com.airport.repository.*;
import com.airport.repository.jdbc.JdbcBookingRepository;
import com.airport.repository.jdbc.JdbcFlightRepository;
import com.airport.repository.jdbc.JdbcPassengerRepository;
import com.airport.service.BookingService;
import com.airport.service.FlightService;
import com.airport.service.PassengerService;
import com.airport.service.PassengerService.PassengerFlightInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AirportApp extends Application {
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter T_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private FlightService flightService;
    private PassengerService passengerService;
    private BookingService bookingService;

    private Stage primaryStage;
    private BorderPane shell;
    private StackPane contentHost;
    private Label headerTitle;
    private Label headerSub;
    private Button activeNav;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        try {
            initServices();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Database could not be started:\n" + e.getMessage() +
                            "\n\nCheck src/main/resources/com/airport/db.properties.", ButtonType.OK);
            alert.setHeaderText("Startup failed");
            alert.showAndWait();
            Platform.exit();
            return;
        }
        primaryStage.setTitle("SkyPort | Airport Management");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
        showLogin();
        primaryStage.show();
    }

    private void initServices() {
        // Force database init (schema + seed) before opening the UI.
        String mode = Database.getMode();
        System.out.println("Database mode: " + mode);

        PassengerRepository passengerRepo = new JdbcPassengerRepository();
        FlightRepository flightRepo = new JdbcFlightRepository();
        BookingRepository bookingRepo = new JdbcBookingRepository();
        this.flightService = new FlightService(flightRepo);
        this.passengerService = new PassengerService(passengerRepo, bookingRepo, flightRepo);
        this.bookingService = new BookingService(bookingRepo, passengerRepo, flightRepo);
    }

    private void applyStyles(Scene scene) {
        URL css = getClass().getResource("/com/airport/ui/app.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
    }

    private void showLogin() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-root");

        Region overlay = new Region();
        overlay.getStyleClass().add("login-overlay");
        overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox card = new VBox(14);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER_LEFT);
        StackPane.setMargin(card, new Insets(24));

        Label badge = new Label("✈  SkyPort Control");
        badge.setStyle("-fx-background-color:#dbeafe; -fx-text-fill:#1d4ed8; -fx-padding:6 10; -fx-background-radius:999; -fx-font-weight:bold;");
        Label title = new Label("Airport Management System");
        title.getStyleClass().add("app-title");
        Label sub = new Label("Sign in to manage passengers, flights, and boarding operations.");
        sub.getStyleClass().add("subtitle");
        sub.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(100);
        form.getColumnConstraints().add(c1);

        Label userLbl = new Label("Username");
        userLbl.getStyleClass().add("detail-label");
        TextField userField = new TextField("admin");
        userField.setPromptText("Enter username");
        Label passLbl = new Label("Password");
        passLbl.getStyleClass().add("detail-label");
        PasswordField passField = new PasswordField();
        passField.setText("admin123");
        passField.setPromptText("Enter password");

        Label error = new Label();
        error.getStyleClass().add("error-label");
        Label hint = new Label("Demo access: admin / admin123  •  Database: " + Database.getMode());
        hint.getStyleClass().add("muted");
        hint.setWrapText(true);
        hint.setMaxWidth(400);

        HBox banner = new HBox(hint);
        banner.getStyleClass().add("db-banner");

        Button loginBtn = new Button("Sign in to Dashboard");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        form.add(userLbl, 0, 0);
        form.add(userField, 0, 1);
        form.add(passLbl, 0, 2);
        form.add(passField, 0, 3);
        form.add(loginBtn, 0, 4);
        form.add(error, 0, 5);

        Runnable doLogin = () -> {
            if ("admin".equals(userField.getText()) && "admin123".equals(passField.getText())) {
                showMain();
            } else {
                error.setText("Invalid credentials. Please try again.");
            }
        };
        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());
        userField.setOnAction(e -> passField.requestFocus());

        card.getChildren().addAll(badge, title, sub, new Separator(), form, banner, error);
        root.getChildren().addAll(overlay, card);
        Scene scene = new Scene(root, 1180, 760);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    private void showMain() {
        shell = new BorderPane();

        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        Label brand = new Label("✈  SkyPort");
        brand.getStyleClass().add("brand");
        Label brandSub = new Label("Operations Console");
        brandSub.setStyle("-fx-text-fill:#94a3b8; -fx-padding:0 8 10 8;");

        Button dashBtn = navButton("📊  Dashboard");
        Button flightsBtn = navButton("🛫  Flights");
        Button paxBtn = navButton("👥  Passengers");
        Button bookingsBtn = navButton("🎫  Bookings");
        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        Button logout = navButton("⎋  Logout");
        logout.setOnAction(e -> showLogin());

        dashBtn.setOnAction(e -> { setActiveNav(dashBtn); showDashboard(); });
        flightsBtn.setOnAction(e -> { setActiveNav(flightsBtn); showFlights(); });
        paxBtn.setOnAction(e -> { setActiveNav(paxBtn); showPassengers(); });
        bookingsBtn.setOnAction(e -> { setActiveNav(bookingsBtn); showBookings(); });

        sidebar.getChildren().addAll(brand, brandSub, new Separator(), dashBtn, flightsBtn, paxBtn, bookingsBtn, grow, new Separator(), logout);

        VBox top = new VBox();
        top.getStyleClass().add("header");
        headerTitle = new Label("Dashboard");
        headerTitle.getStyleClass().add("header-title");
        headerSub = new Label("Live overview of airport operations (" + Database.getMode() + ")");
        headerSub.getStyleClass().add("header-sub");
        top.getChildren().addAll(headerTitle, headerSub);

        contentHost = new StackPane();
        contentHost.getStyleClass().add("content");

        Region contentOverlay = new Region();
        contentOverlay.getStyleClass().add("content-overlay");
        contentOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentHost.getChildren().add(contentOverlay);

        BorderPane center = new BorderPane();
        center.setTop(top);
        center.setCenter(contentHost);
        shell.setLeft(sidebar);
        shell.setCenter(center);

        Scene scene = new Scene(shell, 1200, 760);
        applyStyles(scene);
        primaryStage.setScene(scene);

        setActiveNav(dashBtn);
        showDashboard();
    }

    private Button navButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("nav-button");
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    private void setActiveNav(Button b) {
        if (activeNav != null) activeNav.getStyleClass().remove("nav-button-active");
        activeNav = b;
        if (!b.getStyleClass().contains("nav-button-active")) b.getStyleClass().add("nav-button-active");
    }

    private void setContent(Region node) {
        if (contentHost.getChildren().size() > 1) {
            contentHost.getChildren().set(1, node);
        } else {
            contentHost.getChildren().add(node);
        }
    }

    private void setHeader(String title, String sub) {
        headerTitle.setText(title);
        headerSub.setText(sub);
    }

    private void showDashboard() {
        setHeader("Dashboard", "Monitor upcoming departures, boarding activity, and passenger volume");
        VBox root = new VBox(18);

        List<Flight> upcoming = flightService.getUpcomingFlights();
        List<Flight> boarding = flightService.getBoardingNowFlights();
        long delayed = flightService.getAllFlights().stream().filter(f -> f.getStatus() == FlightStatus.DELAYED).count();
        int totalPax = passengerService.getAllPassengers().size();

        HBox cards = new HBox(14);
        cards.getChildren().addAll(
                statCard("Upcoming Flights", String.valueOf(upcoming.size()), "Scheduled to depart", "#2563eb"),
                statCard("Boarding / Check-in", String.valueOf(boarding.size()), "Active right now", "#15803d"),
                statCard("Delayed Flights", String.valueOf(delayed), "Needs attention", "#d97706"),
                statCard("Total Passengers", String.valueOf(totalPax), "In system records", "#0f172a")
        );

        VBox tableCard = new VBox(10);
        tableCard.getStyleClass().add("card");
        Label nextLbl = new Label("Next Departures & Boarding");
        nextLbl.getStyleClass().add("section-title");
        Label hint = new Label("Sorted by boarding time so staff can prioritize gates and announcements.");
        hint.getStyleClass().add("muted");
        TableView<Flight> table = buildFlightTable();
        table.setItems(FXCollections.observableArrayList(upcoming));
        table.setPrefHeight(420);
        VBox.setVgrow(table, Priority.ALWAYS);
        tableCard.getChildren().addAll(nextLbl, hint, table);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        root.getChildren().addAll(cards, tableCard);
        VBox.setVgrow(root, Priority.ALWAYS);
        setContent(root);
    }

    private VBox statCard(String title, String value, String caption, String accent) {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        box.getStyleClass().add("stat-card");
        box.setPrefWidth(240);
        HBox.setHgrow(box, Priority.ALWAYS);
        Region accentBar = new Region();
        accentBar.getStyleClass().add("stat-accent");
        accentBar.setStyle("-fx-background-color: " + accent + ";");
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Label v = new Label(value);
        v.getStyleClass().add("card-value");
        Label c = new Label(caption);
        c.getStyleClass().add("muted");
        box.getChildren().addAll(accentBar, t, v, c);
        return box;
    }

    private void showFlights() {
        setHeader("Flights", "Track schedules, boarding times, gates, and live flight status");
        BorderPane root = new BorderPane();
        root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(0, 0, 12, 0));
        Label fLbl = new Label("View:");
        fLbl.getStyleClass().add("detail-label");
        ComboBox<String> filter = new ComboBox<>(FXCollections.observableArrayList("All Flights", "Upcoming", "Boarding / Check-in"));
        filter.setValue("All Flights");
        filter.setPrefWidth(200);
        Button refresh = new Button("Refresh");
        refresh.getStyleClass().add("secondary-button");
        Button newBtn = new Button("+ New Flight");
        newBtn.getStyleClass().add("primary-button");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("secondary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-button");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label tip = new Label("CRUD operations write directly to the database");
        tip.getStyleClass().add("muted");
        controls.getChildren().addAll(fLbl, filter, refresh, new Separator(), newBtn, editBtn, deleteBtn, spacer, tip);

        TableView<Flight> table = buildFlightTable();
        VBox tableCard = wrapCard(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox detailCard = new VBox(10);
        detailCard.getStyleClass().add("card");
        detailCard.setPrefWidth(340);
        Label dTitle = new Label("Flight Details");
        dTitle.getStyleClass().add("section-title");
        GridPane detailGrid = new GridPane();
        detailGrid.setHgap(10);
        detailGrid.setVgap(8);
        detailCard.getChildren().addAll(dTitle, detailGrid);
        renderFlightDetails(detailGrid, null);

        Runnable load = () -> {
            String f = filter.getValue();
            List<Flight> data;
            if ("Upcoming".equals(f)) data = flightService.getUpcomingFlights();
            else if ("Boarding / Check-in".equals(f)) data = flightService.getBoardingNowFlights();
            else data = flightService.getAllFlights();
            table.setItems(FXCollections.observableArrayList(data));
        };
        filter.setOnAction(e -> load.run());
        refresh.setOnAction(e -> load.run());
        load.run();

        newBtn.setOnAction(e -> FormDialogs.flight(null, primaryStage).ifPresent(f -> {
            try {
                flightService.createFlight(f);
                load.run();
                showInfo("Flight " + f.getFlightNumber() + " created in the database.");
            } catch (Exception ex) { showError(ex); }
        }));
        editBtn.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a flight to edit."); return; }
            FormDialogs.flight(selected, primaryStage).ifPresent(f -> {
                try {
                    flightService.updateFlight(f);
                    load.run();
                    showInfo("Flight " + f.getFlightNumber() + " updated.");
                } catch (Exception ex) { showError(ex); }
            });
        });
        deleteBtn.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a flight to delete."); return; }
            if (confirmDelete("flight " + selected.getFlightNumber())) {
                try {
                    flightService.deleteFlight(selected.getFlightId());
                    load.run();
                } catch (Exception ex) { showError(ex); }
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, fl) -> renderFlightDetails(detailGrid, fl));

        SplitPane split = new SplitPane(tableCard, detailCard);
        split.setDividerPositions(0.7);
        root.setTop(controls);
        root.setCenter(split);
        setContent(root);
    }

    private void renderFlightDetails(GridPane grid, Flight fl) {
        grid.getChildren().clear();
        if (fl == null) {
            Label hint = new Label("Select a flight from the table to see airline, route, departure, boarding time, gate, terminal, and status.");
            hint.getStyleClass().add("muted");
            hint.setWrapText(true);
            grid.add(hint, 0, 0, 2, 1);
            return;
        }
        int r = 0;
        addDetailRow(grid, r++, "Flight", fl.getFlightNumber() + "  (" + fl.getFlightId() + ")");
        addDetailRow(grid, r++, "Airline", fl.getAirline());
        addDetailRow(grid, r++, "Route", fl.getRoute());
        addDetailRow(grid, r++, "Departure", fmt(fl.getDepartureTime()));
        addDetailRow(grid, r++, "Arrival", fmt(fl.getArrivalTime()));
        addDetailRow(grid, r++, "Boarding Time", fmt(fl.getBoardingTime()));
        addDetailRow(grid, r++, "Boarding In", boardingInText(fl));
        addDetailRow(grid, r++, "Gate / Terminal", safe(fl.getGate()) + "  ·  " + safe(fl.getTerminal()));
        Label status = new Label(formatStatus(fl.getStatus()));
        status.getStyleClass().addAll(statusStyleClass(fl.getStatus()).split(" "));
        grid.add(boldLabel("Status"), 0, r);
        grid.add(status, 1, r);
    }

    private void showPassengers() {
        setHeader("Passengers", "Search passengers and review their bookings, boarding times, and flight status");
        BorderPane root = new BorderPane();

        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 12, 0));
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name, passenger ID, passport, or email");
        searchField.setPrefWidth(380);
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("primary-button");
        Button clearBtn = new Button("Clear");
        clearBtn.getStyleClass().add("secondary-button");
        Button newBtn = new Button("+ New Passenger");
        newBtn.getStyleClass().add("primary-button");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("secondary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-button");
        Label countLbl = new Label();
        countLbl.getStyleClass().add("muted");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        searchBox.getChildren().addAll(new Label("Find passenger:"), searchField, searchBtn, clearBtn,
                new Separator(), newBtn, editBtn, deleteBtn, sp, countLbl);

        TableView<Passenger> pTable = new TableView<>();
        pTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        pTable.getColumns().addAll(
                col("ID", c -> c.getValue().getPassengerId()),
                col("Name", c -> c.getValue().getFullName()),
                col("Email", c -> c.getValue().getEmail()),
                col("Phone", c -> c.getValue().getPhone()),
                col("Passport/ID", c -> c.getValue().getPassportOrId())
        );
        VBox pCard = wrapCard(pTable);
        VBox.setVgrow(pTable, Priority.ALWAYS);

        VBox right = new VBox(12);
        VBox detailCard = new VBox(8);
        detailCard.getStyleClass().add("card");
        Label dTitle = new Label("Passenger Details");
        dTitle.getStyleClass().add("section-title");
        GridPane dGrid = new GridPane();
        dGrid.setHgap(10);
        dGrid.setVgap(6);
        Label emptyP = new Label("Select a passenger to view profile and bookings.");
        emptyP.getStyleClass().add("muted");
        emptyP.setWrapText(true);
        dGrid.add(emptyP, 0, 0, 2, 1);
        detailCard.getChildren().addAll(dTitle, dGrid);

        VBox bCard = new VBox(8);
        bCard.getStyleClass().add("card");
        Label bTitle = new Label("Bookings & Boarding Information");
        bTitle.getStyleClass().add("section-title");
        TableView<PassengerFlightInfo> bTable = new TableView<>();
        bTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        bTable.getColumns().addAll(
                infoCol("PNR", c -> c.getValue().getBooking().getBookingId()),
                infoCol("Flight", c -> c.getValue().getFlight().getFlightNumber()),
                infoCol("Route", c -> c.getValue().getFlight().getRoute()),
                infoCol("Boarding Time", c -> fmt(c.getValue().getFlight().getBoardingTime())),
                infoCol("Gate", c -> c.getValue().getFlight().getGate()),
                infoCol("Seat", c -> c.getValue().getBooking().getSeatNumber()),
                infoCol("Class", c -> c.getValue().getBooking().getTravelClass()),
                infoCol("Check-in", c -> c.getValue().getBooking().getCheckInStatus().name()),
                infoCol("Flight Status", c -> formatStatus(c.getValue().getFlight().getStatus()))
        );
        bTable.setPlaceholder(new Label("No bookings found for this passenger."));
        VBox.setVgrow(bTable, Priority.ALWAYS);
        bCard.getChildren().addAll(bTitle, bTable);
        VBox.setVgrow(bCard, Priority.ALWAYS);
        right.getChildren().addAll(detailCard, bCard);
        VBox.setVgrow(right, Priority.ALWAYS);

        Runnable loadPassengers = () -> {
            List<Passenger> list = passengerService.searchPassengers(searchField.getText());
            pTable.setItems(FXCollections.observableArrayList(list));
            countLbl.setText(list.size() + " passenger(s)");
        };
        searchBtn.setOnAction(e -> loadPassengers.run());
        searchField.setOnAction(e -> loadPassengers.run());
        clearBtn.setOnAction(e -> { searchField.clear(); loadPassengers.run(); });
        loadPassengers.run();

        newBtn.setOnAction(e -> FormDialogs.passenger(null, primaryStage).ifPresent(p -> {
            try {
                passengerService.createPassenger(p);
                loadPassengers.run();
                showInfo("Passenger " + p.getFullName() + " created in the database.");
            } catch (Exception ex) { showError(ex); }
        }));
        editBtn.setOnAction(e -> {
            Passenger selected = pTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a passenger to edit."); return; }
            FormDialogs.passenger(selected, primaryStage).ifPresent(p -> {
                try {
                    passengerService.updatePassenger(p);
                    loadPassengers.run();
                    showInfo("Passenger " + p.getFullName() + " updated.");
                } catch (Exception ex) { showError(ex); }
            });
        });
        deleteBtn.setOnAction(e -> {
            Passenger selected = pTable.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a passenger to delete."); return; }
            if (confirmDelete("passenger " + selected.getFullName())) {
                try {
                    passengerService.deletePassenger(selected.getPassengerId());
                    loadPassengers.run();
                } catch (Exception ex) { showError(ex); }
            }
        });

        pTable.getSelectionModel().selectedItemProperty().addListener((obs, o, p) -> {
            dGrid.getChildren().clear();
            if (p == null) {
                Label hint = new Label("Select a passenger to view profile and bookings.");
                hint.getStyleClass().add("muted");
                dGrid.add(hint, 0, 0, 2, 1);
                bTable.getItems().clear();
                return;
            }
            int r = 0;
            addDetailRow(dGrid, r++, "Name", p.getFullName() + " (" + p.getPassengerId() + ")");
            addDetailRow(dGrid, r++, "DOB / Gender", safe(String.valueOf(p.getDob())) + "  ·  " + safe(p.getGender()));
            addDetailRow(dGrid, r++, "Phone", safe(p.getPhone()));
            addDetailRow(dGrid, r++, "Email", safe(p.getEmail()));
            addDetailRow(dGrid, r++, "Passport/ID", safe(p.getPassportOrId()));
            bTable.setItems(FXCollections.observableArrayList(passengerService.getPassengerFlights(p.getPassengerId())));
        });

        SplitPane split = new SplitPane(pCard, right);
        split.setDividerPositions(0.46);
        root.setTop(searchBox);
        root.setCenter(split);
        setContent(root);
    }

    private void showBookings() {
        setHeader("Bookings", "Create, edit and delete PNR bookings stored in the database");
        BorderPane root = new BorderPane();

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(0, 0, 12, 0));
        Button refresh = new Button("Refresh");
        refresh.getStyleClass().add("secondary-button");
        Button newBtn = new Button("+ New Booking");
        newBtn.getStyleClass().add("primary-button");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("secondary-button");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("secondary-button");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label hint = new Label("Bookings link passengers to flights and are persisted via JDBC.");
        hint.getStyleClass().add("muted");
        controls.getChildren().addAll(refresh, new Separator(), newBtn, editBtn, deleteBtn, spacer, hint);

        final Map<String, String>[] paxNames = new Map[]{Map.of()};
        final Map<String, String>[] flightLabels = new Map[]{Map.of()};

        TableView<Booking> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No bookings found."));
        table.getColumns().addAll(
                col("PNR", c -> c.getValue().getBookingId()),
                col("Passenger", c -> paxNames[0].getOrDefault(c.getValue().getPassengerId(), c.getValue().getPassengerId())),
                col("Flight", c -> flightLabels[0].getOrDefault(c.getValue().getFlightId(), c.getValue().getFlightId())),
                col("Seat", c -> c.getValue().getSeatNumber()),
                col("Class", c -> c.getValue().getTravelClass()),
                col("Check-in", c -> c.getValue().getCheckInStatus() == null ? "-" : c.getValue().getCheckInStatus().name()),
                col("Booking Status", c -> c.getValue().getBookingStatus())
        );
        VBox card = wrapCard(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        Runnable load = () -> {
            paxNames[0] = bookingService.getAllPassengers().stream().collect(Collectors.toMap(
                    Passenger::getPassengerId, p -> p.getFullName() + " (" + p.getPassengerId() + ")", (a, b) -> a));
            flightLabels[0] = bookingService.getAllFlights().stream().collect(Collectors.toMap(
                    Flight::getFlightId, f -> f.getFlightNumber() + " (" + f.getRoute() + ")", (a, b) -> a));
            table.setItems(FXCollections.observableArrayList(bookingService.getAllBookings()));
        };
        refresh.setOnAction(e -> load.run());
        load.run();

        newBtn.setOnAction(e -> {
            List<Passenger> passengers = bookingService.getAllPassengers();
            List<Flight> flights = bookingService.getAllFlights();
            if (passengers.isEmpty() || flights.isEmpty()) {
                showInfo("Create at least one passenger and one flight before adding a booking.");
                return;
            }
            FormDialogs.booking(null, passengers, flights, primaryStage).ifPresent(b -> {
                try {
                    bookingService.createBooking(b);
                    load.run();
                    showInfo("Booking " + b.getBookingId() + " created in the database.");
                } catch (Exception ex) { showError(ex); }
            });
        });
        editBtn.setOnAction(e -> {
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a booking to edit."); return; }
            FormDialogs.booking(selected, bookingService.getAllPassengers(), bookingService.getAllFlights(), primaryStage)
                    .ifPresent(b -> {
                        try {
                            bookingService.updateBooking(b);
                            load.run();
                            showInfo("Booking " + b.getBookingId() + " updated.");
                        } catch (Exception ex) { showError(ex); }
                    });
        });
        deleteBtn.setOnAction(e -> {
            Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { showInfo("Select a booking to delete."); return; }
            if (confirmDelete("booking " + selected.getBookingId())) {
                try {
                    bookingService.deleteBooking(selected.getBookingId());
                    load.run();
                } catch (Exception ex) { showError(ex); }
            }
        });

        root.setTop(controls);
        root.setCenter(card);
        setContent(root);
    }

    private TableView<Flight> buildFlightTable() {
        TableView<Flight> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No flights to display."));

        table.getColumns().addAll(
                flightCol("Flight", f -> f.getFlightNumber()),
                flightCol("Airline", Flight::getAirline),
                flightCol("Route", Flight::getRoute),
                flightCol("Departure", f -> fmt(f.getDepartureTime())),
                flightCol("Boarding Time", f -> fmt(f.getBoardingTime())),
                flightCol("Gate", Flight::getGate),
                flightCol("Terminal", Flight::getTerminal),
                flightCol("Status", f -> formatStatus(f.getStatus()))
        );
        return table;
    }

    private TableColumn<Flight, String> flightCol(String title, java.util.function.Function<Flight, String> mapper) {
        TableColumn<Flight, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(mapper.apply(c.getValue())));
        return col;
    }

    private <T> TableColumn<T, String> col(String title, java.util.function.Function<TableColumn.CellDataFeatures<T, String>, String> mapper) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(mapper.apply(cd)));
        return c;
    }

    private TableColumn<PassengerFlightInfo, String> infoCol(String title, java.util.function.Function<TableColumn.CellDataFeatures<PassengerFlightInfo, String>, String> mapper) {
        TableColumn<PassengerFlightInfo, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(mapper.apply(cd)));
        return c;
    }

    private VBox wrapCard(Region inner) {
        VBox card = new VBox(inner);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(8));
        VBox.setVgrow(inner, Priority.ALWAYS);
        return card;
    }

    private void addDetailRow(GridPane grid, int row, String label, String value) {
        grid.add(boldLabel(label), 0, row);
        Label v = new Label(value == null ? "-" : value);
        v.setWrapText(true);
        grid.add(v, 1, row);
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("detail-label");
        l.setMinWidth(110);
        return l;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
        alert.setHeaderText("Database operation failed");
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    private boolean confirmDelete(String what) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + what + "? This change is committed to the database immediately.",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Confirm delete");
        alert.initOwner(primaryStage);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private String formatStatus(FlightStatus status) {
        if (status == null) return "-";
        return switch (status) {
            case CHECK_IN_OPEN -> "Check-in Open";
            case BOARDING -> "Boarding";
            case DELAYED -> "Delayed";
            case DEPARTED -> "Departed";
            case CANCELLED -> "Cancelled";
            case SCHEDULED -> "Scheduled";
        };
    }

    private String statusStyleClass(FlightStatus status) {
        return switch (status) {
            case BOARDING -> "status-pill status-boarding";
            case CHECK_IN_OPEN -> "status-pill status-check-in";
            case DELAYED -> "status-pill status-delayed";
            case CANCELLED -> "status-pill status-cancelled";
            case DEPARTED -> "status-pill status-departed";
            case SCHEDULED -> "status-pill status-scheduled";
            default -> "status-pill status-scheduled";
        };
    }

    private String boardingInText(Flight fl) {
        if (fl == null || fl.getBoardingTime() == null) return "-";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime b = fl.getBoardingTime();
        if (b.isBefore(now)) return "Started / passed (" + T_FMT.format(b) + ")";
        long mins = java.time.Duration.between(now, b).toMinutes();
        if (mins < 60) return "In " + mins + " min";
        long h = mins / 60; long m = mins % 60;
        return "In " + h + "h " + m + "m";
    }

    private String fmt(LocalDateTime dt) {
        return dt == null ? "-" : DT_FMT.format(dt);
    }

    private String safe(String s) { return s == null || s.isBlank() ? "-" : s; }

    public static void main(String[] args) {
        launch(args);
    }
}
