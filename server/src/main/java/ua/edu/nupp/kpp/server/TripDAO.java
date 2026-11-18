package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці trip. Містить лише SQL/JDBC, без предметних правил.
 */
public class TripDAO {

    private static final String COLUMNS = "id, request_id, vehicle_id, trip_date, collected_volume, note";

    public void add(Trip t) throws SQLException {
        String sql = "INSERT INTO trip(request_id, vehicle_id, trip_date, collected_volume, note) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, t);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Trip> getAll() throws SQLException {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM trip ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Trip getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM trip WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Trip t) throws SQLException {
        String sql = "UPDATE trip SET request_id = ?, vehicle_id = ?, trip_date = ?, collected_volume = ?, note = ? "
                + "WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, t);
            p.setLong(6, t.getId());
            p.executeUpdate();
        }
    }

    /** Кількість виїздів за заявкою (для правил життєвого циклу і захисту від видалення). */
    public int countByRequestId(long requestId) throws SQLException {
        return count("SELECT COUNT(*) FROM trip WHERE request_id = ?", requestId);
    }

    /** Кількість виїздів транспортного засобу (для захисту від видалення). */
    public int countByVehicleId(long vehicleId) throws SQLException {
        return count("SELECT COUNT(*) FROM trip WHERE vehicle_id = ?", vehicleId);
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM trip WHERE id = ?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private int count(String sql, long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private void bind(PreparedStatement p, Trip t) throws SQLException {
        p.setLong(1, t.getRequestId());
        p.setLong(2, t.getVehicleId());
        p.setString(3, t.getTripDate());
        if (t.getCollectedVolume() == null) {
            p.setNull(4, Types.DOUBLE);
        } else {
            p.setDouble(4, t.getCollectedVolume());
        }
        p.setString(5, t.getNote());
    }

    private Trip map(ResultSet r) throws SQLException {
        double volume = r.getDouble("collected_volume");
        Double collectedVolume = r.wasNull() ? null : volume;
        return new Trip(
                r.getLong("id"),
                r.getLong("request_id"),
                r.getLong("vehicle_id"),
                r.getString("trip_date"),
                collectedVolume,
                r.getString("note"));
    }
}
