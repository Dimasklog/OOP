package ru.example;

/**
 * Исключение отсутствия остановки: остановка не входит в последовательность остановок маршрута.
 */
public class TransportStopNotInRouteException extends RuntimeException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public TransportStopNotInRouteException(String message) {
        super(message);
    }
}
