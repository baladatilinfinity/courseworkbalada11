package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;

/**
 * Головне вікно JavaFX-клієнта: чотири вкладки для основних сутностей.
 * Усі операції виконуються через HttpWasteClient (HTTP → Servlet → Service → DAO → MySQL);
 * після кожної зміни дані повторно завантажуються із сервера.
 */
public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private final HttpWasteClient api = new HttpWasteClient();

    private final TableView<ContainerSite> sites = new TableView<>();
    private final TableView<PickupRequest> requests = new TableView<>();
    private final TableView<Vehicle> vehicles = new TableView<>();
    private final TableView<Trip> trips = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Майданчики", sitesPane()),
                new Tab("Заявки", requestsPane()),
                new Tab("Транспорт", vehiclesPane()),
                new Tab("Виїзди", tripsPane()));
        tabs.getTabs().forEach(tab -> tab.setClosable(false));

        stage.setScene(new Scene(tabs, 1200, 680));
        stage.setTitle("Керування заявками на вивезення відходів");
        stage.show();
        refreshAll();
    }

    // =====================================================================
    // Вкладка «Майданчики»
    // =====================================================================
    private Node sitesPane() {
        sites.getColumns().setAll(
                col("ID", "id"),
                col("Код", "code"),
                col("Адреса", "address"),
                col("Район", "district"),
                col("Контейнерів", "containerCount"),
                col("Місткість, м³", "containerVolume"),
                col("Інтервал, днів", "pickupIntervalDays"));

        TextField code = new TextField();
        TextField address = new TextField();
        TextField district = new TextField();
        TextField count = new TextField();
        TextField volume = new TextField();
        TextField interval = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addSite(new ContainerSite(null,
                    required(code, "Код"), required(address, "Адреса"), required(district, "Район"),
                    toInt(count, "Кількість контейнерів"), toDouble(volume, "Місткість контейнера"),
                    toInt(interval, "Інтервал вивезення")));
            refreshSites();
        }));
        update.setOnAction(e -> safe(() -> {
            ContainerSite s = selected(sites);
            api.updateSite(new ContainerSite(s.getId(),
                    required(code, "Код"), required(address, "Адреса"), required(district, "Район"),
                    toInt(count, "Кількість контейнерів"), toDouble(volume, "Місткість контейнера"),
                    toInt(interval, "Інтервал вивезення")));
            refreshSites();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteSite(selected(sites).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshSites());

        sites.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s != null) {
                code.setText(s.getCode());
                address.setText(s.getAddress());
                district.setText(s.getDistrict());
                count.setText(String.valueOf(s.getContainerCount()));
                volume.setText(String.valueOf(s.getContainerVolume()));
                interval.setText(String.valueOf(s.getPickupIntervalDays()));
            }
        });

        return layout(sites, form(
                new Label("Код майданчика"), code,
                new Label("Адреса"), address,
                new Label("Район"), district,
                new Label("Кількість контейнерів"), count,
                new Label("Місткість контейнера, м³"), volume,
                new Label("Нормативний інтервал вивезення, днів"), interval,
                buttons(add, update, delete, refresh)));
    }

    // =====================================================================
    // Вкладка «Заявки» (життєвий цикл і показник пріоритетності)
    // =====================================================================
    private Node requestsPane() {
        requests.getColumns().setAll(
                col("ID", "id"),
                col("ID майданчика", "siteId"),
                col("Дата", "createdDate"),
                col("Заповнення, %", "fillLevel"),
                col("Сан. оцінка", "sanitaryScore"),
                col("Днів без вивезення", "daysSincePickup"),
                col("Стан", "state"),
                col("F1", "f1"),
                col("F2", "f2"),
                col("F3", "f3"),
                col("I (пріоритет)", "priorityIndex"));

        TextField siteId = new TextField();
        DatePicker date = new DatePicker(LocalDate.now());
        TextField fill = new TextField();
        TextField sanitary = new TextField();
        TextField days = new TextField();
        ComboBox<RequestState> state = new ComboBox<>(FXCollections.observableArrayList(RequestState.values()));
        state.setPromptText("Виберіть стан");

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Button changeState = new Button("Змінити стан");

        add.setOnAction(e -> safe(() -> {
            api.addRequest(readRequest(null, siteId, date, fill, sanitary, days));
            refreshRequests();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateRequest(readRequest(selected(requests).getId(), siteId, date, fill, sanitary, days));
            refreshRequests();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteRequest(selected(requests).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshRequests());
        changeState.setOnAction(e -> safe(() -> {
            PickupRequest r = selected(requests);
            if (state.getValue() == null) {
                throw new IllegalArgumentException("Виберіть новий стан заявки");
            }
            api.changeRequestState(r.getId(), state.getValue());
            refreshRequests();
        }));

        requests.getSelectionModel().selectedItemProperty().addListener((obs, old, r) -> {
            if (r != null) {
                siteId.setText(String.valueOf(r.getSiteId()));
                date.setValue(parseDate(r.getCreatedDate()));
                fill.setText(String.valueOf(r.getFillLevel()));
                sanitary.setText(String.valueOf(r.getSanitaryScore()));
                days.setText(String.valueOf(r.getDaysSincePickup()));
            }
        });

        Label hint = new Label("Стан, F1–F3 та показник I розраховує сервер.");
        hint.setWrapText(true);

        return layout(requests, form(
                new Label("ID майданчика"), siteId,
                new Label("Дата заявки"), date,
                new Label("Рівень заповнення, % (0–100)"), fill,
                new Label("Санітарна оцінка (0–100)"), sanitary,
                new Label("Днів від останнього вивезення"), days,
                buttons(add, update, delete, refresh),
                new Separator(),
                new Label("Новий стан вибраної заявки"), state,
                changeState,
                hint));
    }

    private PickupRequest readRequest(Long id, TextField siteId, DatePicker date,
                                      TextField fill, TextField sanitary, TextField days) {
        PickupRequest r = new PickupRequest();
        r.setId(id);
        r.setSiteId(toLong(siteId, "ID майданчика"));
        r.setCreatedDate(dateText(date, "Дата заявки"));
        r.setFillLevel(toDouble(fill, "Рівень заповнення"));
        r.setSanitaryScore(toDouble(sanitary, "Санітарна оцінка"));
        r.setDaysSincePickup(toInt(days, "Днів від останнього вивезення"));
        return r;
    }

    // =====================================================================
    // Вкладка «Транспорт»
    // =====================================================================
    private Node vehiclesPane() {
        vehicles.getColumns().setAll(
                col("ID", "id"),
                col("Держ. номер", "plateNumber"),
                col("Модель", "model"),
                col("Місткість, м³", "capacityM3"),
                col("Водій", "driverName"));

        TextField plate = new TextField();
        TextField model = new TextField();
        TextField capacity = new TextField();
        TextField driver = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addVehicle(new Vehicle(null, required(plate, "Держ. номер"), required(model, "Модель"),
                    toDouble(capacity, "Місткість кузова"), required(driver, "Водій")));
            refreshVehicles();
        }));
        update.setOnAction(e -> safe(() -> {
            Vehicle v = selected(vehicles);
            api.updateVehicle(new Vehicle(v.getId(), required(plate, "Держ. номер"), required(model, "Модель"),
                    toDouble(capacity, "Місткість кузова"), required(driver, "Водій")));
            refreshVehicles();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteVehicle(selected(vehicles).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshVehicles());

        vehicles.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (v != null) {
                plate.setText(v.getPlateNumber());
                model.setText(v.getModel());
                capacity.setText(String.valueOf(v.getCapacityM3()));
                driver.setText(v.getDriverName());
            }
        });

        return layout(vehicles, form(
                new Label("Державний номер"), plate,
                new Label("Модель"), model,
                new Label("Місткість кузова, м³"), capacity,
                new Label("Водій (ПІБ)"), driver,
                buttons(add, update, delete, refresh)));
    }

    // =====================================================================
    // Вкладка «Виїзди»
    // =====================================================================
    private Node tripsPane() {
        trips.getColumns().setAll(
                col("ID", "id"),
                col("ID заявки", "requestId"),
                col("ID транспорту", "vehicleId"),
                col("Дата виїзду", "tripDate"),
                col("Вивезено, м³", "collectedVolume"),
                col("Примітка", "note"));

        TextField requestId = new TextField();
        TextField vehicleId = new TextField();
        DatePicker date = new DatePicker(LocalDate.now());
        TextField volume = new TextField();
        volume.setPromptText("порожньо — ще не виконано");
        TextField note = new TextField();

        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addTrip(readTrip(null, requestId, vehicleId, date, volume, note));
            refreshTrips();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateTrip(readTrip(selected(trips).getId(), requestId, vehicleId, date, volume, note));
            refreshTrips();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteTrip(selected(trips).getId());
            refreshTrips();
        }));
        refresh.setOnAction(e -> refreshTrips());

        trips.getSelectionModel().selectedItemProperty().addListener((obs, old, t) -> {
            if (t != null) {
                requestId.setText(String.valueOf(t.getRequestId()));
                vehicleId.setText(String.valueOf(t.getVehicleId()));
                date.setValue(parseDate(t.getTripDate()));
                volume.setText(t.getCollectedVolume() == null ? "" : String.valueOf(t.getCollectedVolume()));
                note.setText(t.getNote() == null ? "" : t.getNote());
            }
        });

        return layout(trips, form(
                new Label("ID заявки"), requestId,
                new Label("ID транспортного засобу"), vehicleId,
                new Label("Дата виїзду"), date,
                new Label("Вивезений обсяг, м³ (необов'язково)"), volume,
                new Label("Примітка (необов'язково)"), note,
                buttons(add, update, delete, refresh)));
    }

    private Trip readTrip(Long id, TextField requestId, TextField vehicleId, DatePicker date,
                          TextField volume, TextField note) {
        String noteText = note.getText() == null || note.getText().isBlank() ? null : note.getText().trim();
        return new Trip(id,
                toLong(requestId, "ID заявки"),
                toLong(vehicleId, "ID транспортного засобу"),
                dateText(date, "Дата виїзду"),
                toOptionalDouble(volume, "Вивезений обсяг"),
                noteText);
    }

    // =====================================================================
    // Оновлення даних із сервера
    // =====================================================================
    private void refreshAll() {
        refreshSites();
        refreshRequests();
        refreshVehicles();
        refreshTrips();
    }

    private void refreshSites() {
        safe(() -> sites.setItems(FXCollections.observableArrayList(api.getSites())));
    }

    private void refreshRequests() {
        safe(() -> requests.setItems(FXCollections.observableArrayList(api.getRequests())));
    }

    private void refreshVehicles() {
        safe(() -> vehicles.setItems(FXCollections.observableArrayList(api.getVehicles())));
    }

    private void refreshTrips() {
        safe(() -> trips.setItems(FXCollections.observableArrayList(api.getTrips())));
    }

    // =====================================================================
    // Перевірка введення на клієнті (формат полів; предметні правила — на сервері)
    // =====================================================================
    private static String required(TextField field, String title) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Заповніть поле «" + title + "»");
        }
        return value;
    }

    private static long toLong(TextField field, String title) {
        try {
            return Long.parseLong(required(field, title));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має містити ціле число");
        }
    }

    private static int toInt(TextField field, String title) {
        try {
            return Integer.parseInt(required(field, title));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має містити ціле число");
        }
    }

    private static double toDouble(TextField field, String title) {
        try {
            return Double.parseDouble(required(field, title).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має містити число");
        }
    }

    private static Double toOptionalDouble(TextField field, String title) {
        if (field.getText() == null || field.getText().isBlank()) {
            return null;
        }
        return toDouble(field, title);
    }

    private static String dateText(DatePicker picker, String title) {
        if (picker.getValue() == null) {
            throw new IllegalArgumentException("Виберіть значення поля «" + title + "»");
        }
        return picker.getValue().toString();   // формат РРРР-ММ-ДД
    }

    private static LocalDate parseDate(String value) {
        try {
            return value == null ? null : LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // =====================================================================
    // Допоміжні методи інтерфейсу
    // =====================================================================
    private interface UiAction {
        void run() throws Exception;
    }

    /** Виконує дію; будь-яку помилку показує користувачу у вікні JavaFX Alert. */
    private void safe(UiAction action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(e.getMessage());
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR,
                message == null ? "Невідома помилка" : message, ButtonType.OK);
        dialog.setHeaderText("Операцію не виконано");
        dialog.showAndWait();
    }

    private static <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Спочатку виберіть запис у таблиці");
        }
        return value;
    }

    private static <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    private static FlowPane buttons(Button... buttons) {
        FlowPane pane = new FlowPane(8, 8, buttons);
        pane.setPadding(new Insets(6, 0, 0, 0));
        return pane;
    }

    private static Node form(Node... nodes) {
        VBox box = new VBox(6, nodes);
        box.setPadding(new Insets(4, 12, 4, 4));
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private static Node layout(TableView<?> table, Node form) {
        table.setPlaceholder(new Label("Немає даних"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.68);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
