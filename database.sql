-- =====================================================================
-- Курсова робота з дисципліни «Кросплатформене програмування»
-- Варіант №7. Інформаційна система керування заявками на вивезення відходів
-- Скрипт повністю відтворює базу даних з нуля: структуру і демонстраційні дані.
-- =====================================================================

DROP DATABASE IF EXISTS waste_collection;
CREATE DATABASE waste_collection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE waste_collection;

-- ---------------------------------------------------------------------
-- Контейнерний майданчик
-- pickup_interval_days — нормативний інтервал вивезення (днів),
-- використовується для нормування фактора F3 (прострочення).
-- ---------------------------------------------------------------------
CREATE TABLE container_site (
    id                   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    code                 VARCHAR(20)  NOT NULL UNIQUE,
    address              VARCHAR(200) NOT NULL,
    district             VARCHAR(100) NOT NULL,
    container_count      INT          NOT NULL,
    container_volume     DOUBLE       NOT NULL,
    pickup_interval_days INT          NOT NULL,
    CHECK (container_count > 0),
    CHECK (container_volume > 0),
    CHECK (pickup_interval_days > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Транспортний засіб (сміттєвоз)
-- ---------------------------------------------------------------------
CREATE TABLE vehicle (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20)  NOT NULL UNIQUE,
    model        VARCHAR(100) NOT NULL,
    capacity_m3  DOUBLE       NOT NULL,
    driver_name  VARCHAR(120) NOT NULL,
    CHECK (capacity_m3 > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Заявка на вивезення відходів — об'єкт життєвого циклу
-- Стани: CREATED → SCHEDULED → IN_PROGRESS → COMPLETED
-- F1 — рівень заповнення; F2 — санітарний ризик; F3 — прострочення;
-- priority_index = 0,40·F1 + 0,35·F2 + 0,25·F3 (розраховує сервер).
-- ---------------------------------------------------------------------
CREATE TABLE pickup_request (
    id                BIGINT      AUTO_INCREMENT PRIMARY KEY,
    site_id           BIGINT      NOT NULL,
    created_date      DATE        NOT NULL,
    fill_level        DOUBLE      NOT NULL,
    sanitary_score    DOUBLE      NOT NULL,
    days_since_pickup INT         NOT NULL,
    state             VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    f1                DOUBLE      NOT NULL,
    f2                DOUBLE      NOT NULL,
    f3                DOUBLE      NOT NULL,
    priority_index    DOUBLE      NOT NULL,
    CONSTRAINT fk_request_site FOREIGN KEY (site_id) REFERENCES container_site (id),
    CHECK (fill_level BETWEEN 0 AND 100),
    CHECK (sanitary_score BETWEEN 0 AND 100),
    CHECK (days_since_pickup >= 0),
    CHECK (state IN ('CREATED', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED')),
    CHECK (f1 BETWEEN 0 AND 100),
    CHECK (f2 BETWEEN 0 AND 100),
    CHECK (f3 BETWEEN 0 AND 100),
    CHECK (priority_index BETWEEN 0 AND 100)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Виїзд транспортного засобу за заявкою
-- collected_volume заповнюється після виконання виїзду (до того NULL).
-- ---------------------------------------------------------------------
CREATE TABLE trip (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    request_id       BIGINT       NOT NULL,
    vehicle_id       BIGINT       NOT NULL,
    trip_date        DATE         NOT NULL,
    collected_volume DOUBLE       NULL,
    note             VARCHAR(255) NULL,
    CONSTRAINT fk_trip_request FOREIGN KEY (request_id) REFERENCES pickup_request (id),
    CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle (id),
    CHECK (collected_volume IS NULL OR collected_volume >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Початкові демонстраційні дані
-- ---------------------------------------------------------------------
INSERT INTO container_site (code, address, district, container_count, container_volume, pickup_interval_days) VALUES
('KM-001', 'вул. Соборності, 12',      'Шевченківський', 4, 1.10, 2),
('KM-002', 'вул. Європейська, 45',     'Київський',      3, 1.10, 3),
('KM-003', 'вул. Небесної Сотні, 8',   'Подільський',    6, 0.75, 1);

INSERT INTO vehicle (plate_number, model, capacity_m3, driver_name) VALUES
('ВІ1234АВ', 'МАЗ-5337 КО-440', 16.0, 'Петренко Олег Васильович'),
('ВІ5678СЕ', 'Renault Midlum',  12.0, 'Коваль Ігор Миколайович');

-- F1 = fill_level; F2 = sanitary_score;
-- F3 = min(100; max(0; days_since_pickup − interval) / interval · 100)
-- Заявка 1: F1 = 95, F2 = 70, F3 = (4 − 2) / 2 · 100 = 100   → I = 87.50
-- Заявка 2: F1 = 80, F2 = 40, F3 = (4 − 3) / 3 · 100 = 33.33 → I = 54.33
-- Заявка 3: F1 = 60, F2 = 85, F3 = (1 − 1) / 1 · 100 = 0     → I = 53.75
INSERT INTO pickup_request (site_id, created_date, fill_level, sanitary_score, days_since_pickup, state, f1, f2, f3, priority_index) VALUES
(1, '2026-11-10', 95.0, 70.0, 4, 'COMPLETED', 95.0, 70.0, 100.00, 87.50),
(2, '2026-11-14', 80.0, 40.0, 4, 'SCHEDULED', 80.0, 40.0,  33.33, 54.33),
(3, '2026-11-16', 60.0, 85.0, 1, 'CREATED',   60.0, 85.0,   0.00, 53.75);

INSERT INTO trip (request_id, vehicle_id, trip_date, collected_volume, note) VALUES
(1, 1, '2026-11-11', 4.2,  'Вивезено повністю'),
(2, 2, '2026-11-18', NULL, 'Запланований виїзд');
