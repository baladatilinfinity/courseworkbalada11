package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка для транспортних засобів: валідація, унікальність державного номера,
 * захист від видалення засобу, задіяного у виїздах.
 */
public class VehicleService {

    private static final Logger LOG = Logger.getLogger(VehicleService.class.getName());

    private final VehicleDAO dao = new VehicleDAO();
    private final TripDAO tripDAO = new TripDAO();

    public List<Vehicle> getAll() throws SQLException {
        return dao.getAll();
    }

    public Vehicle getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Vehicle v) throws SQLException {
        validate(v);
        if (dao.getByPlateNumber(v.getPlateNumber()) != null) {
            throw reject("Транспортний засіб з номером " + v.getPlateNumber() + " вже існує");
        }
        dao.add(v);
        LOG.info("Додано транспортний засіб " + v.getPlateNumber());
    }

    public void update(Vehicle v) throws SQLException {
        validate(v);
        if (v.getId() == null || dao.getById(v.getId()) == null) {
            throw reject("Транспортний засіб не знайдено");
        }
        Vehicle samePlate = dao.getByPlateNumber(v.getPlateNumber());
        if (samePlate != null && !samePlate.getId().equals(v.getId())) {
            throw reject("Транспортний засіб з номером " + v.getPlateNumber() + " вже існує");
        }
        dao.update(v);
        LOG.info("Оновлено транспортний засіб " + v.getPlateNumber());
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw reject("Транспортний засіб не знайдено");
        }
        if (tripDAO.countByVehicleId(id) > 0) {
            throw reject("Неможливо видалити транспортний засіб: він задіяний у виїздах");
        }
        dao.delete(id);
        LOG.info("Видалено транспортний засіб №" + id);
    }

    static void validate(Vehicle v) {
        if (v == null
                || !ValidationUtils.isValidText(v.getPlateNumber(), 20)
                || !ValidationUtils.isValidText(v.getModel(), 100)
                || !ValidationUtils.isValidText(v.getDriverName(), 120)) {
            throw reject("Номер, модель і водій є обов'язковими (номер до 20, модель до 100, ПІБ до 120 символів)");
        }
        if (!ValidationUtils.isPositive(v.getCapacityM3())) {
            throw reject("Місткість кузова має бути більшою за 0");
        }
    }

    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
