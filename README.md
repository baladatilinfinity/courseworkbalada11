# Інформаційна система керування заявками на вивезення відходів

Курсова робота з дисципліни «Кросплатформене програмування».
Варіант №7. Виконав: Гомля Артем Юрійович, група 201-ТН.

> Проєкт у розробці. Етап 1: структура Maven-проєктів, база даних, підключення JDBC, початковий CSV.

## Вимоги до середовища

- JDK 21, Apache Maven
- MySQL / MariaDB (XAMPP), phpMyAdmin
- Apache Tomcat 9.x (з наступних етапів)

## Структура

```
kpp-coursework-homlia-variant07/
├─ database.sql              — створення БД waste_collection з нуля
├─ server/                   — серверний Maven-проєкт (Servlet, Service, DAO, JDBC)
│  └─ src/main/java/ua/edu/nupp/kpp/server/DBUtil.java
└─ client/                   — клієнтський Maven-проєкт (JavaFX)
   └─ sample-data/container_sites.csv
```

## Створення бази даних

1. Запустити Apache і MySQL у XAMPP Control Panel.
2. Відкрити http://localhost/phpmyadmin → вкладка «Імпорт» → вибрати `database.sql` → «Вперед».
3. Перевірити базу `waste_collection` і таблиці `container_site`, `vehicle`, `pickup_request`, `trip`.

## Параметри підключення

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

## Перевірка складання

```
cd server
mvn clean compile
cd ../client
mvn clean compile
```
