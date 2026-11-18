package ua.edu.nupp.kpp.server;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Спільні допоміжні перевірки для Service-класів.
 * Методи лише повертають результат перевірки; рішення і повідомлення про помилку формує Service.
 */
final class ValidationUtils {

    private ValidationUtils() {
    }

    /** Рядок відсутній або складається лише з пробілів. */
    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Обов'язковий рядок, довжина якого не перевищує розмір стовпця VARCHAR. */
    static boolean isValidText(String value, int maxLength) {
        return !isBlank(value) && value.length() <= maxLength;
    }

    /** Скінченне додатне число (NaN і нескінченність не допускаються). */
    static boolean isPositive(double value) {
        return Double.isFinite(value) && value > 0;
    }

    /** Число в межах [min; max] (NaN не допускається). */
    static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /** Розбирає дату у форматі yyyy-MM-dd; повертає null, якщо формат некоректний. */
    static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
