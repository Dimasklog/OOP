package org.example.blackjack.model;

import java.math.BigDecimal;

/**
 * Деньги.
 * @param amount количество денег
 */
public record Money(BigDecimal amount) {
    /**
     * Создает объект денег(ставки, запас денег и т.д.)
     * @param amount количество денег
     * @throws IllegalArgumentException если {@code amount == null} или {@code amount.compareTo(BigDecimal.ZERO) < 0}
     */
    public Money{
        if (amount == null){
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("amount must be non-negative");
        }
        amount = amount.stripTrailingZeros();
    }
}
