package org.example.blackjack.model.enums;

import org.example.blackjack.model.Money;

import java.math.BigDecimal;

/**
 * Результаты руки с расчетом полного возврата денег.
 * Выплата включает возвращаемую собственную ставку.
 */
public enum HandOutcome {

    /** Проигрыш без выплаты. */
    LOSS("0"),

    /** Ничья с возвратом ставки. */
    PUSH("1"),

    /** Обычная победа с возвратом двух ставок. */
    WIN("2"),

    /** Начальный блэкджек с прибылью 3:2 и возвратом ставки. */
    BLACKJACK("2.5"),

    /** Сдача с возвратом половины ставки. */
    SURRENDER("0.5");

    private final BigDecimal multiplier;

    /**
     * Создает исход с указанным множителем выплаты.
     *
     * @param multiplier множитель выплаты в виде строки
     */
    HandOutcome(String multiplier) {
        this.multiplier = new BigDecimal(multiplier);
    }

    /**
     * Рассчитывает полный возврат денег по ставке.
     *
     * @param bet ставка руки
     * @return выплата, включая возвращаемую ставку
     * @throws IllegalArgumentException если ставка null
     *                                  или не больше нуля
     */
    public Money payout(Money bet) {
        if (bet == null) {
            throw new IllegalArgumentException("bet must not be null");
        }
        if (bet.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("bet must be positive");
        }

        return bet.multiply(multiplier);
    }
}