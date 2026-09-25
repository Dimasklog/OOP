package ru.example;

/**
 * Исключение чужого рейса: рейс не принадлежит маршруту, для которого вызвана операция.
 */
public class ForeignTripException extends RuntimeException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public ForeignTripException(String message) {
        super(message);
    }
}
