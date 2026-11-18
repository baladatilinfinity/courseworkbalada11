package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка виїздів: валідація, перевірка заявки і транспортного засобу,
 * узгодженість дати та обсягу з даними пов'язаних записів.
 */
public class TripService {

    private static final Logger LOG = Logger.getLogger(TripService.class.getName());

    private final TripDAO dao = new TripDAO();
    private final PickupRequestDAO requestDAO = new PickupRequestDAO();
    private final VehicleDAO vehicleDAO = new VehicleDAO();

    public List<Trip> getAll() throws SQLException {
        return dao.getAll();
    }

    public Trip getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Trip t) throws SQLException {
        validate(t);
        PickupRequest request = requireRequest(t.getRequestId());
        if (request.getState() == RequestState.COMPLETED) {
            throw reject("Не можна призначити виїзд для виконаної заявки №" + request.getId());
        }
        checkRelated(t, request, requireVehicle(t.getVehicleId()));
        dao.add(t);
        LOG.info("Призначено виїзд №" + t.getId() + " за заявкою №" + t.getRequestId());
    }

    public void update(Trip t) throws SQLException {
        validate(t);
        if (t.getId() == null || dao.getById(t.getId()) == null) {
            throw reject("Виїзд не знайдено");
        }
        checkRelated(t, requireRequest(t.getRequestId()), requireVehicle(t.getVehicleId()));
        dao.update(t);
        LOG.info("Оновлено виїзд №" + t.getId());
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw reject("Виїзд не знайдено");
        }
        dao.delete(id);
        LOG.info("Видалено виїзд №" + id);
    }

    static void validate(Trip t) {
        if (t == null || t.getRequestId() <= 0 || t.getVehicleId() <= 0) {
            throw reject("Для виїзду потрібно вказати заявку і транспортний засіб");
        }
        if (ValidationUtils.parseDate(t.getTripDate()) == null) {
            throw reject("Дата виїзду має бути у форматі РРРР-ММ-ДД");
        }
        Double volume = t.getCollectedVolume();
        if (volume != null && !(Double.isFinite(volume) && volume >= 0)) {
            throw reject("Вивезений обсяг не може бути від'ємним");
        }
        if (t.getNote() != null && t.getNote().length() > 255) {
            throw reject("Примітка не може перевищувати 255 символів");
        }
    }

    /** Дата виїзду не раніше дати заявки; обсяг не більший за місткість транспортного засобу. */
    private static void checkRelated(Trip t, PickupRequest request, Vehicle vehicle) {
        LocalDate tripDate = ValidationUtils.parseDate(t.getTripDate());
        LocalDate requestDate = ValidationUtils.parseDate(request.getCreatedDate());
        if (requestDate != null && tripDate.isBefore(requestDate)) {
            throw reject("Дата виїзду не може бути раніше дати створення заявки");
        }
        if (t.getCollectedVolume() != null && t.getCollectedVolume() > vehicle.getCapacityM3()) {
            throw reject("Вивезений обсяг перевищує місткість транспортного засобу");
        }
    }

    private PickupRequest requireRequest(long id) throws SQLException {
        PickupRequest request = requestDAO.getById(id);
        if (request == null) {
            throw reject("Заявку не знайдено");
        }
        return request;
    }

    private Vehicle requireVehicle(long id) throws SQLException {
        Vehicle vehicle = vehicleDAO.getById(id);
        if (vehicle == null) {
            throw reject("Транспортний засіб не знайдено");
        }
        return vehicle;
    }

    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
