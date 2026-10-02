package ru.example;

/**
 * Транспортная остановка.
 * @param code идентификатор, код остановки, положительный
 * @param name имя остановки, не пустое
 */

public record TransportStop(long code, String name) {
    /**
     * Создаёт транспортную остановку.
     * @param code идентификатор, код остановки, положительный
     * @param name имя остановки, не пустое
     * @throws IllegalArgumentException если code не положительное или name null, или пустое
     */
    public TransportStop{
        if (code <= 0){
            throw new IllegalArgumentException("code must be positive");
        }
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("name must be non-blank");
        }
    }
}
