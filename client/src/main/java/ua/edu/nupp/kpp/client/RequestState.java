package ua.edu.nupp.kpp.client;

/**
 * Стани заявки на клієнті. Назви констант збігаються із серверними (для JSON і HTTP),
 * а toString() повертає український підпис для таблиці та списку вибору.
 * Допустимість переходів клієнт не перевіряє — це робить сервер.
 */
public enum RequestState {
    CREATED("Створено"),
    SCHEDULED("Заплановано"),
    IN_PROGRESS("Виконується"),
    COMPLETED("Виконано");

    private final String title;

    RequestState(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
