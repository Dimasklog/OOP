package ru.example;

/**
 * Транспортная остановка.
 * @param code идентификатор, код остановки, положительный
 * @param name имя остановки, не пустое
 */

public record TransportStop(long code, String name) {
    public TransportStop{
        if (code <= 0){
            throw new IllegalArgumentException("codeOfRoute must be positive");
        }
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("name must be non-blank");
        }
    }
}
