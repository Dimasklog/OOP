package org.example;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.Hand;
import org.example.blackjack.model.HandEvaluator;
import org.example.blackjack.model.enums.Rank;
import org.example.blackjack.model.enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты оценщика руки.
 */
class HandEvaluatorTests {

    private final HandEvaluator evaluator = new HandEvaluator();

    /**
     * Пустая рука даёт 0 очков.
     */
    @Test
    void emptyHandTest() {
        Hand hand = new Hand();

        assertEquals(0, evaluator.evaluate(hand));
    }

    /**
     * Стоимости обычных карт складываются.
     */
    @Test
    void ordinaryCardsTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.FIVE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.KING, Suit.SPADES));

        assertEquals(21, evaluator.evaluate(hand));
    }

    /**
     * Туз считается за 11, когда перебора нет.
     */
    @Test
    void aceAsElevenTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));

        assertEquals(17, evaluator.evaluate(hand));
    }

    /**
     * При необходимости туз считается за 1.
     */
    @Test
    void aceAsOneTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.SIX, Suit.CLUBS));
        hand.addCard(new Card(Rank.TEN, Suit.SPADES));

        assertEquals(17, evaluator.evaluate(hand));
    }

    /**
     * Из двух тузов достаточно уменьшить стоимость одного.
     */
    @Test
    void twoAcesTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        hand.addCard(new Card(Rank.NINE, Suit.SPADES));

        assertEquals(21, evaluator.evaluate(hand));
    }

    /**
     * Все три туза могут учитываться по 1.
     */
    @Test
    void threeAcesTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));
        hand.addCard(new Card(Rank.ACE, Suit.SPADES));
        hand.addCard(new Card(Rank.NINE, Suit.DIAMONDS));

        assertEquals(12, evaluator.evaluate(hand));
    }

    /**
     * Один туз нельзя уменьшить несколько раз.
     */
    @Test
    void aceCannotBeReducedTwiceTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.ACE, Suit.HEARTS));
        hand.addCard(new Card(Rank.KING, Suit.CLUBS));
        hand.addCard(new Card(Rank.KING, Suit.SPADES));
        hand.addCard(new Card(Rank.FIVE, Suit.DIAMONDS));

        assertEquals(26, evaluator.evaluate(hand));
    }

    /**
     * Перебор без тузов возвращается без изменений.
     */
    @Test
    void bustWithoutAcesTest() {
        Hand hand = new Hand();
        hand.addCard(new Card(Rank.KING, Suit.HEARTS));
        hand.addCard(new Card(Rank.QUEEN, Suit.CLUBS));
        hand.addCard(new Card(Rank.FIVE, Suit.SPADES));

        assertEquals(25, evaluator.evaluate(hand));
    }

    /**
     * Отсутствующая рука вызывает исключение.
     */
    @Test
    void nullHandThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluate(null));
    }
}