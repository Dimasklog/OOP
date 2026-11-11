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
    /**
     * Сложение сохраняет точность и не меняет исходные суммы.
     */
    @Test
    void addTest() {
        Money first = new Money(new BigDecimal("10"));
        Money second = new Money(new BigDecimal("2.5"));

        Money result = first.add(second);

        assertEquals(new Money(new BigDecimal("12.5")), result);
        assertEquals(new Money(new BigDecimal("10")), first);
        assertEquals(new Money(new BigDecimal("2.5")), second);
    }

    /**
     * Нельзя прибавить отсутствующую сумму.
     */
    @Test
    void addNullThrows() {
        Money money = new Money(new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class,
                () -> money.add(null));
    }

    /**
     * Вычитание даёт правильную разность и допускает нулевой результат.
     */
    @Test
    void subtractTest() {
        Money money = new Money(new BigDecimal("10"));
        Money other = new Money(new BigDecimal("2.5"));

        assertEquals(
                new Money(new BigDecimal("7.5")),
                money.subtract(other)
        );

        assertEquals(
                new Money(BigDecimal.ZERO),
                money.subtract(money)
        );

        assertEquals(new Money(new BigDecimal("10")), money);
        assertEquals(new Money(new BigDecimal("2.5")), other);
    }

    /**
     * Вычитание большей суммы вызывает исключение.
     */
    @Test
    void subtractLargerAmountThrows() {
        Money money = new Money(new BigDecimal("5"));
        Money other = new Money(new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class,
                () -> money.subtract(other));

        assertEquals(new Money(new BigDecimal("5")), money);
    }

    /**
     * Нельзя вычесть отсутствующую сумму.
     */
    @Test
    void subtractNullThrows() {
        Money money = new Money(new BigDecimal("10"));

        assertThrows(IllegalArgumentException.class,
                () -> money.subtract(null));
    }

    /**
     * Дробный коэффициент даёт точный результат.
     */
    @Test
    void multiplyTest() {
        Money money = new Money(new BigDecimal("5"));

        Money result = money.multiply(new BigDecimal("1.5"));

        assertEquals(new Money(new BigDecimal("7.5")), result);
        assertEquals(new Money(new BigDecimal("5")), money);
    }

    /**
     * Умножение на ноль разрешено.
     */
    @Test
    void multiplyByZeroTest() {
        Money money = new Money(new BigDecimal("5"));

        assertEquals(
                new Money(BigDecimal.ZERO),
                money.multiply(BigDecimal.ZERO)
        );
    }

    /**
     * Отрицательный коэффициент запрещён даже для нулевой суммы.
     */
    @Test
    void negativeFactorThrows() {
        Money money = new Money(new BigDecimal("5"));
        Money zero = new Money(BigDecimal.ZERO);
        BigDecimal factor = new BigDecimal("-1");

        assertThrows(IllegalArgumentException.class,
                () -> money.multiply(factor));

        assertThrows(IllegalArgumentException.class,
                () -> zero.multiply(factor));
    }

    /**
     * Отсутствующий коэффициент вызывает исключение.
     */
    @Test
    void nullFactorThrows() {
        Money money = new Money(new BigDecimal("5"));

        assertThrows(IllegalArgumentException.class,
                () -> money.multiply(null));
    }
}