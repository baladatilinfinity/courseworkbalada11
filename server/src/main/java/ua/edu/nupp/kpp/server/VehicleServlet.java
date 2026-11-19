package ua.edu.nupp.kpp.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * HTTP-доступ до транспортних засобів: URL /vehicles.
 * GET — список; POST з action=add | update | delete — зміни.
 */
public class VehicleServlet extends JsonServlet {

    private final VehicleService service = new VehicleService();

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

    private Vehicle read(HttpServletRequest req, Long id) {
        return new Vehicle(
                id,
                text(req, "plateNumber"),
                text(req, "model"),
                doubleParam(req, "capacityM3", "Місткість кузова"),
                text(req, "driverName"));
    }
}
