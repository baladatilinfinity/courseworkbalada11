package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Спільна основа для Servlet-класів системи:
 * формування JSON-відповіді, JSON-помилки та перетворення HTTP-параметрів у Java-типи.
 * <p>
 * Предметних правил тут немає — вони залишаються на Service-рівні.
 */
public abstract class JsonServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(JsonServlet.class.getName());

    private final ObjectMapper mapper = new ObjectMapper();

    /** Записує Java-об'єкт або список у тіло відповіді у форматі JSON (UTF-8). */
    protected void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    /**
     * Формує JSON-помилку {"error": "..."}.
     * IllegalArgumentException (некоректні дані, предметне правило) → HTTP 400 з текстом Service;
     * будь-який інший виняток (SQLException тощо) → HTTP 500 і запис у журнал.
     */
    protected void writeError(HttpServletRequest req, HttpServletResponse resp, Exception e) throws IOException {
        int status;
        String message;
        if (e instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = e.getMessage() == null ? "Некоректний запит" : e.getMessage();
        } else {
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Внутрішня помилка сервера";
            LOG.log(Level.SEVERE, "Помилка обробки запиту " + req.getMethod() + " " + req.getRequestURI(), e);
        }
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }

    /** Значення параметра action; відсутній action вважається помилкою запиту. */
    protected static String action(HttpServletRequest req) {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Не задано операцію (параметр action)");
        }
        return action;
    }

    /** Текстовий параметр без зайвих пробілів або null, якщо його не передано чи він порожній. */
    protected static String text(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    protected static long longParam(HttpServletRequest req, String name, String title) {
        String value = text(req, name);
        if (value == null) {
            throw new IllegalArgumentException("Поле «" + title + "» є обов'язковим");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути цілим числом");
        }
    }

    protected static int intParam(HttpServletRequest req, String name, String title) {
        String value = text(req, name);
        if (value == null) {
            throw new IllegalArgumentException("Поле «" + title + "» є обов'язковим");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути цілим числом");
        }
    }

    protected static double doubleParam(HttpServletRequest req, String name, String title) {
        Double value = optionalDoubleParam(req, name, title);
        if (value == null) {
            throw new IllegalArgumentException("Поле «" + title + "» є обов'язковим");
        }
        return value;
    }

    /** Необов'язкове дробове число; кома як десятковий роздільник також допускається. */
    protected static Double optionalDoubleParam(HttpServletRequest req, String name, String title) {
        String value = text(req, name);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + title + "» має бути числом");
        }
    }

    protected static long id(HttpServletRequest req) {
        return longParam(req, "id", "id");
    }
}
