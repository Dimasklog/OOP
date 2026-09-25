package ru.example;

/**
 * Исключение дубликата рейса: в маршруте уже есть рейс с теми же временем отправления и признаком дня.
 */
public class DuplicateTripException extends RuntimeException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public DuplicateTripException(String message) {
        super(message);
    }
}
