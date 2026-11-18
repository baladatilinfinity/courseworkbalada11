package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка для контейнерних майданчиків: валідація, унікальність коду,
 * захист від видалення майданчика, для якого існують заявки.
 */
public class ContainerSiteService {

    private static final Logger LOG = Logger.getLogger(ContainerSiteService.class.getName());

    private final ContainerSiteDAO dao = new ContainerSiteDAO();
    private final PickupRequestDAO requestDAO = new PickupRequestDAO();

    public List<ContainerSite> getAll() throws SQLException {
        return dao.getAll();
    }

    public ContainerSite getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(ContainerSite s) throws SQLException {
        validate(s);
        if (dao.getByCode(s.getCode()) != null) {
            throw reject("Майданчик з кодом " + s.getCode() + " вже існує");
        }
        dao.add(s);
        LOG.info("Додано контейнерний майданчик " + s.getCode());
    }

    public void update(ContainerSite s) throws SQLException {
        validate(s);
        if (s.getId() == null || dao.getById(s.getId()) == null) {
            throw reject("Контейнерний майданчик не знайдено");
        }
        ContainerSite sameCode = dao.getByCode(s.getCode());
        if (sameCode != null && !sameCode.getId().equals(s.getId())) {
            throw reject("Майданчик з кодом " + s.getCode() + " вже існує");
        }
        dao.update(s);
        LOG.info("Оновлено контейнерний майданчик " + s.getCode());
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw reject("Контейнерний майданчик не знайдено");
        }
        if (requestDAO.countBySiteId(id) > 0) {
            throw reject("Неможливо видалити майданчик: для нього зареєстровано заявки");
        }
        dao.delete(id);
        LOG.info("Видалено контейнерний майданчик №" + id);
    }

    static void validate(ContainerSite s) {
        if (s == null
                || !ValidationUtils.isValidText(s.getCode(), 20)
                || !ValidationUtils.isValidText(s.getAddress(), 200)
                || !ValidationUtils.isValidText(s.getDistrict(), 100)) {
            throw reject("Код, адреса та район майданчика є обов'язковими (код до 20, адреса до 200, район до 100 символів)");
        }
        if (s.getContainerCount() <= 0) {
            throw reject("Кількість контейнерів має бути більшою за 0");
        }
        if (!ValidationUtils.isPositive(s.getContainerVolume())) {
            throw reject("Місткість контейнера має бути більшою за 0");
        }
        if (s.getPickupIntervalDays() <= 0) {
            throw reject("Нормативний інтервал вивезення має бути більшим за 0 днів");
        }
    }

    /** Фіксує відхилену операцію в журналі та повертає виняток для передавання вище. */
    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
