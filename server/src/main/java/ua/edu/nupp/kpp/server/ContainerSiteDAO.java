package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці container_site. Містить лише SQL/JDBC, без предметних правил.
 */
public class ContainerSiteDAO {

    private static final String COLUMNS =
            "id, code, address, district, container_count, container_volume, pickup_interval_days";

    public void add(ContainerSite s) throws SQLException {
        String sql = "INSERT INTO container_site(code, address, district, container_count, "
                + "container_volume, pickup_interval_days) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, s);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<ContainerSite> getAll() throws SQLException {
        List<ContainerSite> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM container_site ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public ContainerSite getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM container_site WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Пошук за унікальним кодом майданчика (потрібен для контролю повторів і CSV-імпорту). */
    public ContainerSite getByCode(String code) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM container_site WHERE code = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, code);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(ContainerSite s) throws SQLException {
        String sql = "UPDATE container_site SET code = ?, address = ?, district = ?, container_count = ?, "
                + "container_volume = ?, pickup_interval_days = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, s);
            p.setLong(7, s.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM container_site WHERE id = ?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, ContainerSite s) throws SQLException {
        p.setString(1, s.getCode());
        p.setString(2, s.getAddress());
        p.setString(3, s.getDistrict());
        p.setInt(4, s.getContainerCount());
        p.setDouble(5, s.getContainerVolume());
        p.setInt(6, s.getPickupIntervalDays());
    }

    private ContainerSite map(ResultSet r) throws SQLException {
        return new ContainerSite(
                r.getLong("id"),
                r.getString("code"),
                r.getString("address"),
                r.getString("district"),
                r.getInt("container_count"),
                r.getDouble("container_volume"),
                r.getInt("pickup_interval_days"));
    }
}
