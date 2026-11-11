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

    /**
     * Добавляет к текущим деньгам другие.
     * @param other другие деньги
     * @return новый объект денег с суммой
     * @throws IllegalArgumentException если {@code other == null}
     */
    public Money add(Money other){
        if (other == null){
            throw new IllegalArgumentException("other must not be null");
        }
        return new Money(amount.add(other.amount()));
    }

    /**
     * Вычитание из текущих денег другие.
     * @param other другие деньги
     * @return новый объект денег с разностью
     * @throws IllegalArgumentException если {@code other == null}, {@code amount.compareTo(other.amount()) < 0}
     */
    public Money subtract(Money other){
        if (other == null){
            throw new IllegalArgumentException("other must not be null");
        }
        if (amount.compareTo(other.amount()) < 0){
            throw new IllegalArgumentException("other must not exceed amount");
        }
        return new Money(amount.subtract(other.amount()));
    }

    /**
     * Перемножение текущих денег на коэффициент.
     * @param factor другие деньги
     * @return новый объект денег с перемножением
     * @throws IllegalArgumentException если {@code factor == null}
     */
    public Money multiply(BigDecimal factor){
        if (factor == null){
            throw new IllegalArgumentException("factor must not be null");
        }
        if (factor.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("factor must be non-negative");
        }
        return new Money(amount.multiply(factor));
    }
}
