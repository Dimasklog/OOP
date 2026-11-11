package org.example;

import org.example.blackjack.model.*;
import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.exceptions.IllegalMoveException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты руки в раздаче.
 */
class RoundHandTests {

    /**
     * Новая рука пустая, активная и сохраняет ставку.
     */
    @Test
    void constructorTest() {
        Money bet = new Money(new BigDecimal("10"));
        RoundHand hand = new RoundHand(bet);

        assertEquals(bet, hand.getBet());
        assertEquals(HandStatus.ACTIVE, hand.getStatus());
        assertEquals(0, hand.getCards().size());
        assertEquals(0, hand.getTotal());
    }

    /**
     * Ставка должна существовать и быть положительной.
     */
    @Test
    void invalidBetThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new RoundHand(null));

        assertThrows(IllegalArgumentException.class,
                () -> new RoundHand(new Money(BigDecimal.ZERO)));
    }

    /**
     * При сумме меньше 21 рука остаётся активной.
     */
    @Test
    void activeHandTest() {
        Money bet = new Money(new BigDecimal("10"));
        RoundHand hand = new RoundHand(bet);

        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.FIVE, Suit.CLUBS));

        assertEquals(15, hand.getTotal());
        assertEquals(HandStatus.ACTIVE, hand.getStatus());
        assertEquals(2, hand.getCards().size());
        assertEquals(bet, hand.getBet());
    }

    /**
     * Уменьшение стоимости туза позволяет продолжать игру.
     */
    @Test
    void aceValueChangesTest() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );

        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));

        assertEquals(17, hand.getTotal());
        assertEquals(HandStatus.ACTIVE, hand.getStatus());
    }

    /**
     * При 21 рука останавливается и больше не принимает карты.
     */
    @Test
    void twentyOneStopsHandTest() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );

        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.KING, Suit.CLUBS));

        assertEquals(21, hand.getTotal());
        assertEquals(HandStatus.STOOD, hand.getStatus());

        List<Card> before = hand.getCards();

        assertThrows(IllegalMoveException.class,
                () -> hand.addCard(new Card(Rank.TWO, Suit.SPADES)));

        assertEquals(before, hand.getCards());
        assertEquals(21, hand.getTotal());
        assertEquals(HandStatus.STOOD, hand.getStatus());
    }

    /**
     * Перебравшая рука больше не принимает карты.
     */
    @Test
    void bustStopsHandTest() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );

        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.KING, Suit.SPADES));

        assertEquals(26, hand.getTotal());
        assertEquals(HandStatus.BUST, hand.getStatus());

        List<Card> before = hand.getCards();

        assertThrows(IllegalMoveException.class,
                () -> hand.addCard(new Card(Rank.TWO, Suit.DIAMONDS)));

        assertEquals(before, hand.getCards());
        assertEquals(26, hand.getTotal());
        assertEquals(HandStatus.BUST, hand.getStatus());
    }

    /**
     * Добавление null не меняет руку.
     */
    @Test
    void nullCardThrows() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );
        Card card = new Card(Rank.SEVEN, Suit.HEARTS);
        hand.addCard(card);

        assertThrows(IllegalArgumentException.class,
                () -> hand.addCard(null));

        assertEquals(List.of(card), hand.getCards());
        assertEquals(7, hand.getTotal());
        assertEquals(HandStatus.ACTIVE, hand.getStatus());
    }
    /**
     * Активную руку можно остановить только один раз.
     */
    @Test
    void standTest() {
        Money bet = new Money(new BigDecimal("10"));
        RoundHand hand = new RoundHand(bet);

        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));

        List<Card> before = hand.getCards();

        hand.stand();

        assertEquals(HandStatus.STOOD, hand.getStatus());
        assertEquals(before, hand.getCards());
        assertEquals(bet, hand.getBet());
        assertEquals(16, hand.getTotal());

        assertThrows(IllegalMoveException.class,
                () -> hand.stand());

        assertEquals(HandStatus.STOOD, hand.getStatus());
        assertEquals(before, hand.getCards());
    }

    /**
     * Нельзя остановиться до получения двух начальных карт.
     */
    @Test
    void standBeforeInitialDealThrows() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );

        assertThrows(IllegalMoveException.class,
                () -> hand.stand());

        assertEquals(HandStatus.ACTIVE, hand.getStatus());
        assertEquals(0, hand.getCards().size());

        Card card = new Card(Rank.SEVEN, Suit.HEARTS);
        hand.addCard(card);

        assertThrows(IllegalMoveException.class,
                () -> hand.stand());

        assertEquals(HandStatus.ACTIVE, hand.getStatus());
        assertEquals(List.of(card), hand.getCards());
    }

    /**
     * Перебравшую руку нельзя остановить.
     */
    @Test
    void standAfterBustThrows() {
        RoundHand hand = new RoundHand(
                new Money(new BigDecimal("10"))
        );

        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.KING, Suit.SPADES));

        List<Card> before = hand.getCards();

        assertThrows(IllegalMoveException.class,
                () -> hand.stand());

        assertEquals(HandStatus.BUST, hand.getStatus());
        assertEquals(26, hand.getTotal());
        assertEquals(before, hand.getCards());
    }
}