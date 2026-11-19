package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP-доступ до виїздів: URL /trips.
 * GET — список; POST з action=add | update | delete — зміни.
 * collectedVolume і note необов'язкові: порожнє значення передається як null.
 */
public class TripServlet extends JsonServlet {

    private final TripService service = new TripService();

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

    private Trip read(HttpServletRequest req, Long id) {
        return new Trip(
                id,
                longParam(req, "requestId", "ID заявки"),
                longParam(req, "vehicleId", "ID транспортного засобу"),
                text(req, "tripDate"),
                optionalDoubleParam(req, "collectedVolume", "Вивезений обсяг"),
                text(req, "note"));
    }
}
