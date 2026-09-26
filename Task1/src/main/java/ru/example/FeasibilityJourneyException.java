package ru.example;

/**
 * Исключение неосуществимой поездки: запрос времени или описания у поездки с пересадкой, не прошедшей проверку осуществимости.
 */
public class FeasibilityJourneyException extends RuntimeException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public FeasibilityJourneyException(String message) {
        super(message);
    }
}
