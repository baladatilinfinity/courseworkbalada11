package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці vehicle. Містить лише SQL/JDBC, без предметних правил.
 */
public class VehicleDAO {

    private static final String COLUMNS = "id, plate_number, model, capacity_m3, driver_name";

    public void add(Vehicle v) throws SQLException {
        String sql = "INSERT INTO vehicle(plate_number, model, capacity_m3, driver_name) VALUES (?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, v);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    v.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Vehicle> getAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM vehicle ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Vehicle getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM vehicle WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Пошук за унікальним державним номером. */
    public Vehicle getByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM vehicle WHERE plate_number = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, plateNumber);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE vehicle SET plate_number = ?, model = ?, capacity_m3 = ?, driver_name = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, v);
            p.setLong(5, v.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM vehicle WHERE id = ?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Vehicle v) throws SQLException {
        p.setString(1, v.getPlateNumber());
        p.setString(2, v.getModel());
        p.setDouble(3, v.getCapacityM3());
        p.setString(4, v.getDriverName());
    }

    private Vehicle map(ResultSet r) throws SQLException {
        return new Vehicle(
                r.getLong("id"),
                r.getString("plate_number"),
                r.getString("model"),
                r.getDouble("capacity_m3"),
                r.getString("driver_name"));
    }
}
