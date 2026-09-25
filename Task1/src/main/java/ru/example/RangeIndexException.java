package ru.example;

/**
 * Исключение индекса остановки: индекс вне диапазона остановок маршрута.
 */
public class RangeIndexException extends RuntimeException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public RangeIndexException(String message) {
        super(message);
    }
}
