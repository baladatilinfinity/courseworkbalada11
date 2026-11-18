package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці pickup_request. Містить лише SQL/JDBC.
 * Не перевіряє допустимість переходів станів і не розраховує показник — це робить Service.
 */
public class PickupRequestDAO {

    private static final String COLUMNS = "id, site_id, created_date, fill_level, sanitary_score, "
            + "days_since_pickup, state, f1, f2, f3, priority_index";

    public void add(PickupRequest r) throws SQLException {
        String sql = "INSERT INTO pickup_request(site_id, created_date, fill_level, sanitary_score, "
                + "days_since_pickup, state, f1, f2, f3, priority_index) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, r);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (keys.next()) {
                    r.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<PickupRequest> getAll() throws SQLException {
        List<PickupRequest> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM pickup_request ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public PickupRequest getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM pickup_request WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(PickupRequest r) throws SQLException {
        String sql = "UPDATE pickup_request SET site_id = ?, created_date = ?, fill_level = ?, sanitary_score = ?, "
                + "days_since_pickup = ?, state = ?, f1 = ?, f2 = ?, f3 = ?, priority_index = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, r);
            p.setLong(11, r.getId());
            p.executeUpdate();
        }
    }

    /** Записує новий стан заявки. Допустимість переходу перевіряє PickupRequestService. */
    public void updateState(long id, RequestState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE pickup_request SET state = ? WHERE id = ?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    /** Кількість заявок, що посилаються на майданчик (для захисту від видалення). */
    public int countBySiteId(long siteId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM pickup_request WHERE site_id = ?")) {
            p.setLong(1, siteId);
            try (ResultSet rs = p.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM pickup_request WHERE id = ?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, PickupRequest r) throws SQLException {
        p.setLong(1, r.getSiteId());
        p.setString(2, r.getCreatedDate());
        p.setDouble(3, r.getFillLevel());
        p.setDouble(4, r.getSanitaryScore());
        p.setInt(5, r.getDaysSincePickup());
        p.setString(6, r.getState().name());
        p.setDouble(7, r.getF1());
        p.setDouble(8, r.getF2());
        p.setDouble(9, r.getF3());
        p.setDouble(10, r.getPriorityIndex());
    }

    private PickupRequest map(ResultSet rs) throws SQLException {
        return new PickupRequest(
                rs.getLong("id"),
                rs.getLong("site_id"),
                rs.getString("created_date"),
                rs.getDouble("fill_level"),
                rs.getDouble("sanitary_score"),
                rs.getInt("days_since_pickup"),
                RequestState.valueOf(rs.getString("state")),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("priority_index"));
    }
}
