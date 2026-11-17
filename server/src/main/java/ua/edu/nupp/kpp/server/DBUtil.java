package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Створення JDBC-з'єднання з базою даних waste_collection.
 * <p>
 * Параметри підключення беруться зі змінних середовища DB_URL, DB_USER, DB_PASSWORD.
 * Якщо змінні не задані, використовуються типові значення локального XAMPP
 * (користувач root без пароля). Справжні паролі в код і в GitHub не записуються.
 */
public final class DBUtil {

    private static final String URL =
            env("DB_URL", "jdbc:mysql://localhost:3306/waste_collection");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
