package org.example.blackjack.model.exceptions;

/**
 * Исключение пустого башмака.
 */
public class ShoeEmptyException extends IllegalStateException{
    public ShoeEmptyException(String message) {
        super(message);
    }
}
