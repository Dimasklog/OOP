package org.example.blackjack.model.exceptions;

/**
 * Исключение недопустимого игрового хода.
 */
public class IllegalMoveException extends IllegalStateException {
    /**
     * Создаёт исключение с пояснением причины.
     *
     * @param message пояснение причины
     */
    public IllegalMoveException(String message) {
        super(message);
    }
}
