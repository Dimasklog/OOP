package org.example;

import org.example.blackjack.model.*;
import org.example.blackjack.model.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты игрока.
 */
class PlayerTests {

    /**
     * Конструктор сохраняет данные игрока.
     */
    @Test
    void constructorTest() {
        PlayerId id = new PlayerId(1);
        Money balance = new Money(new BigDecimal("100"));

        Player player = new Player(id, "Иван", balance);

        assertEquals(id, player.getId());
        assertEquals("Иван", player.getName());
        assertEquals(balance, player.getBalance());
    }

    /**
     * Начальный баланс может быть нулевым.
     */
    @Test
    void zeroBalanceTest() {
        Player player = new Player(
                new PlayerId(1),
                "Иван",
                new Money(BigDecimal.ZERO)
        );

        assertEquals(new Money(BigDecimal.ZERO), player.getBalance());
    }

    /**
     * Списание уменьшает баланс.
     */
    @Test
    void debitTest() {
        Player player = new Player(
                new PlayerId(1),
                "Иван",
                new Money(new BigDecimal("100"))
        );

        player.debit(new Money(new BigDecimal("10.5")));

        assertEquals(
                new Money(new BigDecimal("89.5")),
                player.getBalance()
        );
    }

    /**
     * Начисление увеличивает баланс.
     */
    @Test
    void creditTest() {
        Player player = new Player(
                new PlayerId(1),
                "Иван",
                new Money(new BigDecimal("100"))
        );

        player.credit(new Money(new BigDecimal("7.5")));

        assertEquals(
                new Money(new BigDecimal("107.5")),
                player.getBalance()
        );
    }

    /**
     * Разрешено списать весь баланс.
     */
    @Test
    void debitWholeBalanceTest() {
        Money balance = new Money(new BigDecimal("100"));
        Player player = new Player(new PlayerId(1), "Иван", balance);

        player.debit(balance);

        assertEquals(new Money(BigDecimal.ZERO), player.getBalance());
    }

    /**
     * Неудачное списание не меняет баланс.
     */
    @Test
    void insufficientFundsTest() {
        Money balance = new Money(new BigDecimal("100"));
        Player player = new Player(new PlayerId(1), "Иван", balance);

        assertThrows(InsufficientFundsException.class,
                () -> player.debit(new Money(new BigDecimal("101"))));

        assertEquals(balance, player.getBalance());
    }

    /**
     * Неверные аргументы списания не меняют баланс.
     */
    @Test
    void invalidDebitTest() {
        Money balance = new Money(new BigDecimal("100"));
        Player player = new Player(new PlayerId(1), "Иван", balance);

        assertThrows(IllegalArgumentException.class,
                () -> player.debit(null));
        assertEquals(balance, player.getBalance());

        assertThrows(IllegalArgumentException.class,
                () -> player.debit(new Money(BigDecimal.ZERO)));
        assertEquals(balance, player.getBalance());
    }

    /**
     * Неверные аргументы начисления не меняют баланс.
     */
    @Test
    void invalidCreditTest() {
        Money balance = new Money(new BigDecimal("100"));
        Player player = new Player(new PlayerId(1), "Иван", balance);

        assertThrows(IllegalArgumentException.class,
                () -> player.credit(null));
        assertEquals(balance, player.getBalance());

        assertThrows(IllegalArgumentException.class,
                () -> player.credit(new Money(BigDecimal.ZERO)));
        assertEquals(balance, player.getBalance());
    }

    /**
     * Некорректные данные игрока отклоняются.
     */
    @Test
    void invalidConstructorArgumentsTest() {
        PlayerId id = new PlayerId(1);
        Money balance = new Money(BigDecimal.ZERO);

        assertThrows(IllegalArgumentException.class,
                () -> new Player(null, "Иван", balance));

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, null, balance));

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, "", balance));

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, " \t\n", balance));

        assertThrows(IllegalArgumentException.class,
                () -> new Player(id, "Иван", null));
    }
}