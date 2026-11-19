# Інформаційна система керування заявками на вивезення відходів

Курсова робота з дисципліни «Кросплатформене програмування».
Варіант №7. Виконав: Гомля Артем Юрійович, група 201-ТН.

> Робоча версія Етапу III: Servlet + HTTP/JSON, JavaFX-клієнт із CRUD, початковий CSV-імпорт.
> Повний фоновий CSV-імпорт (JavaFX Task) і тести JUnit 5 — наступний етап.

Маршрут даних: **JavaFX → HttpWasteClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL**

## Вимоги до середовища

- JDK 21, Apache Maven
- MySQL / MariaDB (XAMPP), phpMyAdmin
- Apache Tomcat 9.x (javax.servlet; Tomcat 10+ не підходить)

## Структура

```
courseworkbalada11/
├─ database.sql                       — створення БД waste_collection з нуля
├─ server/                            — серверний Maven-проєкт (WAR)
│  ├─ src/main/java/ua/edu/nupp/kpp/server/   моделі, DAO, Service, Servlet
│  └─ src/main/webapp/WEB-INF/web.xml         URL-прив'язки Servlet
└─ client/                            — клієнтський Maven-проєкт (JavaFX)
   ├─ src/main/java/ua/edu/nupp/kpp/client/   моделі, HttpWasteClient, MainApp, CSV
   └─ sample-data/container_sites.csv
```

## Запуск

1. **MySQL.** Запустити MySQL у XAMPP, у phpMyAdmin імпортувати `database.sql`.
2. **Сервер.**
   ```
   cd server
   mvn clean package
   ```
   Скопіювати `server/target/waste-collection-server.war` у `<Tomcat>/webapps/`, запустити `<Tomcat>/bin/startup.bat`.
   Перевірка: http://localhost:8080/waste-collection-server/sites має повернути JSON.
3. **Клієнт.**
   ```
   cd client
   mvn javafx:run
   ```

## HTTP-маршрути

| URL         | GET              | POST (`action`)                          |
|-------------|------------------|------------------------------------------|
| `/sites`    | список майданчиків | `add`, `update`, `delete`              |
| `/requests` | список заявок    | `add`, `update`, `delete`, `transition`  |
| `/vehicles` | список транспорту | `add`, `update`, `delete`               |
| `/trips`    | список виїздів   | `add`, `update`, `delete`                |

POST-параметри передаються як `application/x-www-form-urlencoded`. Помилки: HTTP 400 (некоректні дані,
предметні правила) або 500 (непередбачена помилка сервера) з JSON `{"error": "..."}`.

## Параметри підключення до БД

Задаються змінними середовища (за замовчуванням — локальний XAMPP, `root` без пароля):

| Змінна        | Значення за замовчуванням                       |
|---------------|-------------------------------------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/waste_collection`  |
| `DB_USER`     | `root`                                          |
| `DB_PASSWORD` | порожній                                        |

## Формат CSV контейнерних майданчиків

Файл `client/sample-data/container_sites.csv`, кодування UTF-8, роздільник `;`, перший рядок — заголовок:

```
code;address;district;container_count;container_volume;pickup_interval_days
```

Початковий імпорт першого рядка: запустити `CsvFirstRowImport` з каталогу `client` (Tomcat має працювати).
