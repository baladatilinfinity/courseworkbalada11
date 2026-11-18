package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка заявок на вивезення відходів:
 * валідація, розрахунок факторів F1–F3 та показника пріоритетності,
 * чотиристанний життєвий цикл CREATED → SCHEDULED → IN_PROGRESS → COMPLETED.
 */
public class PickupRequestService {

    private static final Logger LOG = Logger.getLogger(PickupRequestService.class.getName());

    private final PickupRequestDAO dao = new PickupRequestDAO();
    private final ContainerSiteDAO siteDAO = new ContainerSiteDAO();
    private final TripDAO tripDAO = new TripDAO();

    public List<PickupRequest> getAll() throws SQLException {
        return dao.getAll();
    }

    public PickupRequest getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Створює заявку. Стан, F1–F3 і показник, передані клієнтом, ігноруються:
     * їх задає сервер.
     */
    public void add(PickupRequest r) throws SQLException {
        validate(r);
        ContainerSite site = requireSite(r.getSiteId());
        applyPriority(r, site);
        r.setState(RequestState.CREATED);
        dao.add(r);
        LOG.info("Створено заявку №" + r.getId() + ", показник пріоритетності I = " + r.getPriorityIndex());
    }

    /** Оновлює вхідні дані заявки й перераховує показник. Стан при цьому не змінюється. */
    public void update(PickupRequest r) throws SQLException {
        validate(r);
        PickupRequest current = r.getId() == null ? null : dao.getById(r.getId());
        if (current == null) {
            throw reject("Заявку не знайдено");
        }
        if (current.getState() == RequestState.COMPLETED) {
            throw reject("Виконану заявку не можна редагувати");
        }
        ContainerSite site = requireSite(r.getSiteId());
        applyPriority(r, site);
        r.setState(current.getState());
        dao.update(r);
        LOG.info("Оновлено заявку №" + r.getId() + ", показник пріоритетності I = " + r.getPriorityIndex());
    }

    /** Переводить заявку в наступний стан, якщо перехід дозволений. */
    public void changeState(long id, RequestState next) throws SQLException {
        PickupRequest current = dao.getById(id);
        if (current == null) {
            throw reject("Заявку не знайдено");
        }
        if (!isAllowedTransition(current.getState(), next)) {
            throw reject("Недопустимий перехід стану: " + current.getState() + " -> " + next);
        }
        if (next == RequestState.SCHEDULED && tripDAO.countByRequestId(id) == 0) {
            throw reject("Неможливо запланувати заявку №" + id + " без призначеного виїзду");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан заявки №" + id + ": " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw reject("Заявку не знайдено");
        }
        if (tripDAO.countByRequestId(id) > 0) {
            throw reject("Неможливо видалити заявку №" + id + ": для неї існують виїзди");
        }
        dao.delete(id);
        LOG.info("Видалено заявку №" + id);
    }

    /** Дозволені лише три послідовні переходи; пропуск станів і повернення назад заборонені. */
    public static boolean isAllowedTransition(RequestState from, RequestState to) {
        return (from == RequestState.CREATED && to == RequestState.SCHEDULED)
                || (from == RequestState.SCHEDULED && to == RequestState.IN_PROGRESS)
                || (from == RequestState.IN_PROGRESS && to == RequestState.COMPLETED);
    }

    static void validate(PickupRequest r) {
        if (r == null || r.getSiteId() <= 0) {
            throw reject("Не вказано контейнерний майданчик заявки");
        }
        LocalDate date = ValidationUtils.parseDate(r.getCreatedDate());
        if (date == null) {
            throw reject("Дата заявки має бути у форматі РРРР-ММ-ДД");
        }
        if (date.isAfter(LocalDate.now())) {
            throw reject("Дата заявки не може бути пізніше поточної дати");
        }
        if (!ValidationUtils.isInRange(r.getFillLevel(), 0, 100)) {
            throw reject("Рівень заповнення має бути в діапазоні 0..100 %");
        }
        if (!ValidationUtils.isInRange(r.getSanitaryScore(), 0, 100)) {
            throw reject("Санітарна оцінка має бути в діапазоні 0..100");
        }
        if (r.getDaysSincePickup() < 0) {
            throw reject("Кількість днів від останнього вивезення не може бути від'ємною");
        }
    }

    private ContainerSite requireSite(long siteId) throws SQLException {
        ContainerSite site = siteDAO.getById(siteId);
        if (site == null) {
            throw reject("Контейнерний майданчик не знайдено");
        }
        return site;
    }

    /** Розраховує F1, F2, F3 і показник I на основі даних заявки та нормативу майданчика. */
    private static void applyPriority(PickupRequest r, ContainerSite site) {
        double f1 = PriorityCalculator.factor1(r.getFillLevel());
        double f2 = PriorityCalculator.factor2(r.getSanitaryScore());
        double f3 = PriorityCalculator.factor3(r.getDaysSincePickup(), site.getPickupIntervalDays());
        r.setF1(f1);
        r.setF2(f2);
        r.setF3(f3);
        r.setPriorityIndex(PriorityCalculator.calculate(f1, f2, f3));
    }

    private static IllegalArgumentException reject(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
