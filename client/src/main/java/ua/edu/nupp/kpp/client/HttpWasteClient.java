package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * HTTP-клієнт серверної частини. Ізолює JavaFX від деталей HTTP і JSON:
 * GET — отримання списків, POST (application/x-www-form-urlencoded) — додавання, редагування,
 * видалення і зміна стану заявки. Прямого доступу до MySQL клієнт не має.
 */
public class HttpWasteClient {

    private static final String BASE = "http://localhost:8080/waste-collection-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ---------- Контейнерні майданчики: /sites ----------

    public List<ContainerSite> getSites() {
        return Arrays.asList(read(get("/sites"), ContainerSite[].class));
    }

    public void addSite(ContainerSite s) {
        post("/sites", siteForm("add", s));
    }

    public void updateSite(ContainerSite s) {
        Map<String, String> form = siteForm("update", s);
        form.put("id", String.valueOf(s.getId()));
        post("/sites", form);
    }

    public void deleteSite(long id) {
        post("/sites", deleteForm(id));
    }

    // ---------- Заявки: /requests ----------

    public List<PickupRequest> getRequests() {
        return Arrays.asList(read(get("/requests"), PickupRequest[].class));
    }

    public void addRequest(PickupRequest r) {
        post("/requests", requestForm("add", r));
    }

    public void updateRequest(PickupRequest r) {
        Map<String, String> form = requestForm("update", r);
        form.put("id", String.valueOf(r.getId()));
        post("/requests", form);
    }

    /** Передає серверу бажаний стан; допустимість переходу вирішує сервер. */
    public void changeRequestState(long id, RequestState state) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", "transition");
        form.put("id", String.valueOf(id));
        form.put("state", state.name());
        post("/requests", form);
    }

    public void deleteRequest(long id) {
        post("/requests", deleteForm(id));
    }

    // ---------- Транспортні засоби: /vehicles ----------

    public List<Vehicle> getVehicles() {
        return Arrays.asList(read(get("/vehicles"), Vehicle[].class));
    }

    public void addVehicle(Vehicle v) {
        post("/vehicles", vehicleForm("add", v));
    }

    public void updateVehicle(Vehicle v) {
        Map<String, String> form = vehicleForm("update", v);
        form.put("id", String.valueOf(v.getId()));
        post("/vehicles", form);
    }

    public void deleteVehicle(long id) {
        post("/vehicles", deleteForm(id));
    }

    // ---------- Виїзди: /trips ----------

    public List<Trip> getTrips() {
        return Arrays.asList(read(get("/trips"), Trip[].class));
    }

    public void addTrip(Trip t) {
        post("/trips", tripForm("add", t));
    }

    public void updateTrip(Trip t) {
        Map<String, String> form = tripForm("update", t);
        form.put("id", String.valueOf(t.getId()));
        post("/trips", form);
    }

    public void deleteTrip(long id) {
        post("/trips", deleteForm(id));
    }

    // ---------- Формування параметрів ----------

    private Map<String, String> siteForm(String action, ContainerSite s) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", action);
        form.put("code", s.getCode());
        form.put("address", s.getAddress());
        form.put("district", s.getDistrict());
        form.put("containerCount", String.valueOf(s.getContainerCount()));
        form.put("containerVolume", String.valueOf(s.getContainerVolume()));
        form.put("pickupIntervalDays", String.valueOf(s.getPickupIntervalDays()));
        return form;
    }

    private Map<String, String> requestForm(String action, PickupRequest r) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", action);
        form.put("siteId", String.valueOf(r.getSiteId()));
        form.put("createdDate", r.getCreatedDate());
        form.put("fillLevel", String.valueOf(r.getFillLevel()));
        form.put("sanitaryScore", String.valueOf(r.getSanitaryScore()));
        form.put("daysSincePickup", String.valueOf(r.getDaysSincePickup()));
        return form;
    }

    private Map<String, String> vehicleForm(String action, Vehicle v) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", action);
        form.put("plateNumber", v.getPlateNumber());
        form.put("model", v.getModel());
        form.put("capacityM3", String.valueOf(v.getCapacityM3()));
        form.put("driverName", v.getDriverName());
        return form;
    }

    private Map<String, String> tripForm(String action, Trip t) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", action);
        form.put("requestId", String.valueOf(t.getRequestId()));
        form.put("vehicleId", String.valueOf(t.getVehicleId()));
        form.put("tripDate", t.getTripDate());
        form.put("collectedVolume", t.getCollectedVolume() == null ? "" : String.valueOf(t.getCollectedVolume()));
        form.put("note", t.getNote());
        return form;
    }

    private Map<String, String> deleteForm(long id) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("action", "delete");
        form.put("id", String.valueOf(id));
        return form;
    }

    // ---------- HTTP і JSON ----------

    private String get(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return send(request);
    }

    private String post(String path, Map<String, String> params) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(encode(params), StandardCharsets.UTF_8))
                .build();
        return send(request);
    }

    /** Кодує параметри форми: key1=value1&key2=value2 з URL-кодуванням українських літер і символів. */
    private static String encode(Map<String, String> params) {
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> e : params.entrySet()) {
            String value = e.getValue() == null ? "" : e.getValue();
            joiner.add(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return joiner.toString();
    }

    /** Надсилає запит; HTTP 4xx/5xx перетворює на виняток із текстом поля error від сервера. */
    private String send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (ConnectException e) {
            throw new IllegalStateException("Сервер недоступний. Перевірте, що Tomcat запущено і застосунок розгорнуто.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Помилка зв'язку із сервером: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запит до сервера перервано", e);
        }
        if (response.statusCode() >= 400) {
            throw new IllegalStateException(errorMessage(response));
        }
        return response.body();
    }

    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode node = mapper.readTree(response.body());
            if (node != null && node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (IOException ignored) {
            // тіло відповіді не є JSON — повертаємо загальне повідомлення
        }
        return "Сервер повернув помилку HTTP " + response.statusCode();
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new IllegalStateException("Некоректна JSON-відповідь сервера", e);
        }
    }
}
