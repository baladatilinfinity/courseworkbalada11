package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP-доступ до контейнерних майданчиків: URL /sites.
 * GET — список; POST з action=add | update | delete — зміни.
 */
public class ContainerSiteServlet extends JsonServlet {

    private final ContainerSiteService service = new ContainerSiteService();

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
                case "delete" -> service.delete(id(req));
                default -> throw new IllegalArgumentException("Невідома операція: " + action);
            }
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(req, resp, e);
        }
    }

    private ContainerSite read(HttpServletRequest req, Long id) {
        return new ContainerSite(
                id,
                text(req, "code"),
                text(req, "address"),
                text(req, "district"),
                intParam(req, "containerCount", "Кількість контейнерів"),
                doubleParam(req, "containerVolume", "Місткість контейнера"),
                intParam(req, "pickupIntervalDays", "Інтервал вивезення"));
    }
}
