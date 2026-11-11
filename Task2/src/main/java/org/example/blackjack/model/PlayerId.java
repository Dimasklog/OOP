package org.example.blackjack.model;

/**
 * Идентификационный номер игрока.
 * @param value значение номера
 */
public record PlayerId(long value) {
    /**
     * Создает идентификационный номер игрока.
     * @param value значение номера
     * @throws IllegalArgumentException если {@code value == null}
     */
    public PlayerId{
        if (value <= 0){
            throw new IllegalArgumentException("value must be positive");
        }
    }
}
