package ua.edu.nupp.kpp.client;

/**
 * Розбір рядка CSV-файлу контейнерних майданчиків у клієнтську модель ContainerSite.
 * <p>
 * Формат: UTF-8, роздільник «;», перший рядок — заголовок
 * code;address;district;container_count;container_volume;pickup_interval_days.
 * Рядок із неправильною кількістю полів або некоректними значеннями модель не створює:
 * метод генерує IllegalArgumentException з описом помилки.
 */
public final class SiteCsvParser {

    public static final String HEADER =
            "code;address;district;container_count;container_volume;pickup_interval_days";
    public static final int COLUMNS = 6;

    private SiteCsvParser() {
    }

    /** Перевіряє заголовок файлу (з урахуванням можливого BOM на початку). */
    public static boolean isHeader(String line) {
        return line != null && stripBom(line).trim().equalsIgnoreCase(HEADER);
    }

    public static ContainerSite parseLine(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        String[] parts = stripBom(line).split(";", -1);
        if (parts.length != COLUMNS) {
            throw new IllegalArgumentException("очікується " + COLUMNS + " полів, отримано " + parts.length);
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return new ContainerSite(
                null,
                required(parts[0], "code"),
                required(parts[1], "address"),
                required(parts[2], "district"),
                toInt(parts[3], "container_count"),
                toDouble(parts[4], "container_volume"),
                toInt(parts[5], "pickup_interval_days"));
    }

    private static String required(String value, String column) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException("порожнє поле " + column);
        }
        return value;
    }

    private static int toInt(String value, String column) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле " + column + " має бути цілим числом: «" + value + "»");
        }
    }

    private static double toDouble(String value, String column) {
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле " + column + " має бути числом: «" + value + "»");
        }
    }

    private static String stripBom(String line) {
        return line.startsWith("﻿") ? line.substring(1) : line;
    }
}
