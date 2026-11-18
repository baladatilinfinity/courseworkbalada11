package ua.edu.nupp.kpp.server;

/**
 * Стани життєвого циклу заявки на вивезення відходів:
 * СТВОРЕНО → ЗАПЛАНОВАНО → ВИКОНУЄТЬСЯ → ВИКОНАНО.
 * Правила переходів перевіряє PickupRequestService.
 */
public enum RequestState {
    CREATED,
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED
}
