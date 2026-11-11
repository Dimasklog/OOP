package org.example.blackjack.model.exceptions;

/**
 * Исключение пустого башмака.
 */
public class ShoeEmptyException extends IllegalStateException{
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public ShoeEmptyException(String message) {
        super(message);
    }
}
