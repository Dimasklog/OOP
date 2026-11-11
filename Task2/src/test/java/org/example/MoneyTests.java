package org.example;

import org.example.blackjack.model.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты денежных сумм.
 */
class MoneyTests {

    /**
     * Дробная сумма сохраняется точно.
     */
    @Test
    void positiveAmountTest() {
        Money money = new Money(new BigDecimal("7.5"));

        assertEquals(new BigDecimal("7.5"), money.amount());
    }

    /**
     * Нулевая сумма допустима.
     */
    @Test
    void zeroAmountTest() {
        Money money = new Money(new BigDecimal("0.00"));

        assertEquals(BigDecimal.ZERO, money.amount());
    }

    /**
     * Отсутствующая сумма вызывает исключение.
     */
    @Test
    void nullAmountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Money(null));
    }

    /**
     * Отрицательная сумма вызывает исключение.
     */
    @Test
    void negativeAmountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Money(new BigDecimal("-0.01")));
    }

    /**
     * Лишние нули не влияют на равенство и hashCode.
     */
    @Test
    void equalsAndHashCodeTest() {
        Money first = new Money(new BigDecimal("10"));
        Money second = new Money(new BigDecimal("10.00"));

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    /**
     * Разные суммы не равны.
     */
    @Test
    void differentAmountsTest() {
        Money first = new Money(new BigDecimal("10"));
        Money second = new Money(new BigDecimal("10.01"));

        assertNotEquals(first, second);
    }
}