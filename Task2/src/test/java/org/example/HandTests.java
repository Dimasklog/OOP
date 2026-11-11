package org.example;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.Hand;
import org.example.blackjack.model.enums.Rank;
import org.example.blackjack.model.enums.Suit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты руки.
 */
class HandTests {

    /**
     * Новая рука не содержит карт.
     */
    @Test
    void emptyHandTest() {
        Hand hand = new Hand();

        assertEquals(0, hand.getCards().size());
    }

    /**
     * Добавленная карта сохраняется в руке.
     */
    @Test
    void addCardTest() {
        Hand hand = new Hand();
        Card card = new Card(Rank.ACE, Suit.SPADES);

        hand.addCard(card);

        assertEquals(1, hand.getCards().size());
        assertEquals(card, hand.getCards().get(0));
    }

    /**
     * Карты сохраняют порядок добавления.
     */
    @Test
    void orderTest() {
        Hand hand = new Hand();
        Card first = new Card(Rank.FIVE, Suit.HEARTS);
        Card second = new Card(Rank.KING, Suit.CLUBS);

        hand.addCard(first);
        hand.addCard(second);

        assertEquals(List.of(first, second), hand.getCards());
    }

    /**
     * Одинаковые карты из разных колод допускаются.
     */
    @Test
    void duplicateCardsTest() {
        Hand hand = new Hand();
        Card first = new Card(Rank.ACE, Suit.SPADES);
        Card second = new Card(Rank.ACE, Suit.SPADES);

        hand.addCard(first);
        hand.addCard(second);

        assertEquals(List.of(first, second), hand.getCards());
    }

    /**
     * Добавление null вызывает исключение и не меняет руку.
     */
    @Test
    void nullCardThrows() {
        Hand hand = new Hand();
        Card card = new Card(Rank.TEN, Suit.DIAMONDS);
        hand.addCard(card);

        assertThrows(IllegalArgumentException.class,
                () -> hand.addCard(null));

        assertEquals(List.of(card), hand.getCards());
    }

    /**
     * Полученный список нельзя изменить.
     */
    @Test
    void returnedListCannotBeChanged() {
        Hand hand = new Hand();
        Card first = new Card(Rank.TWO, Suit.CLUBS);
        Card second = new Card(Rank.THREE, Suit.HEARTS);
        hand.addCard(first);

        List<Card> copy = hand.getCards();

        assertThrows(UnsupportedOperationException.class,
                () -> copy.add(second));
        assertThrows(UnsupportedOperationException.class,
                () -> copy.clear());

        assertEquals(List.of(first), hand.getCards());
    }

    /**
     * Добавление карты в руку не меняет старую копию списка.
     */
    @Test
    void previousCopyDoesNotChange() {
        Hand hand = new Hand();
        Card first = new Card(Rank.SEVEN, Suit.HEARTS);
        Card second = new Card(Rank.FOUR, Suit.SPADES);
        hand.addCard(first);

        List<Card> copy = hand.getCards();

        hand.addCard(second);

        assertEquals(List.of(first), copy);
        assertEquals(List.of(first, second), hand.getCards());
    }
}