package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP-доступ до заявок на вивезення: URL /requests.
 * GET — список; POST з action=add | update | delete | transition.
 * <p>
 * Стан, F1–F3 і показник пріоритетності з клієнта не приймаються — їх задає PickupRequestService.
 * Для transition Servlet лише передає бажаний стан; допустимість переходу перевіряє Service.
 */
public class PickupRequestServlet extends JsonServlet {

    private final PickupRequestService service = new PickupRequestService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(req, resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = action(req);
            switch (action) {
                case "add" -> service.add(read(req, null));
                case "update" -> service.update(read(req, id(req)));
                case "transition" -> service.changeState(id(req), state(req));
                case "delete" -> service.delete(id(req));
                default -> throw new IllegalArgumentException("Невідома операція: " + action);
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(req, resp, e);
        }
    }

    private PickupRequest read(HttpServletRequest req, Long id) {
        PickupRequest r = new PickupRequest();
        r.setId(id);
        r.setSiteId(longParam(req, "siteId", "ID майданчика"));
        r.setCreatedDate(text(req, "createdDate"));
        r.setFillLevel(doubleParam(req, "fillLevel", "Рівень заповнення"));
        r.setSanitaryScore(doubleParam(req, "sanitaryScore", "Санітарна оцінка"));
        r.setDaysSincePickup(intParam(req, "daysSincePickup", "Днів від останнього вивезення"));
        return r;
    }

    private RequestState state(HttpServletRequest req) {
        String value = text(req, "state");
        if (value == null) {
            throw new IllegalArgumentException("Не задано новий стан заявки");
        }
        try {
            return RequestState.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан заявки: " + value);
        }
    }
}
