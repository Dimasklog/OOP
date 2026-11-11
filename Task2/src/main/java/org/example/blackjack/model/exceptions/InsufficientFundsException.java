package org.example.blackjack.model.exceptions;

/**
 * Исключение недостатка денег.
 */
public class InsufficientFundsException extends IllegalStateException {
  /**
   * Создаёт исключение с пояснением причины.
   *
   * @param message пояснение причины
   */
    public InsufficientFundsException(String message) {
        super(message);
    }
}
