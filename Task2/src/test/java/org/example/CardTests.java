package org.example;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.enums.Rank;
import org.example.blackjack.model.enums.Suit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты карты.
 */
class CardTests {

    /**
     * Карта сохраняет переданные достоинство и масть.
     */
    @Test
    void constructorTest() {
        Card card = new Card(Rank.QUEEN, Suit.HEARTS);

        assertEquals(Rank.QUEEN, card.rank());
        assertEquals(Suit.HEARTS, card.suit());
    }

    /**
     * Равенство карт и соответствие их hashCode.
     */
    @Test
    void equalsAndHashCodeTest() {
        Card first = new Card(Rank.ACE, Suit.SPADES);
        Card second = new Card(Rank.ACE, Suit.SPADES);
        Card third = new Card(Rank.ACE, Suit.SPADES);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(second, third);
        assertEquals(first, third);

        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(second.hashCode(), third.hashCode());

        assertNotEquals(first, null);
        assertNotEquals(first, "ACE SPADES");
    }

    /**
     * Разные достоинства дают разные карты,
     * даже если стоимость одинакова.
     */
    @Test
    void differentRanksTest() {
        Card first = new Card(Rank.TEN, Suit.HEARTS);
        Card second = new Card(Rank.QUEEN, Suit.HEARTS);

        assertNotEquals(first, second);
    }

    /**
     * Карты с разными мастями не равны.
     */
    @Test
    void differentSuitsTest() {
        Card first = new Card(Rank.ACE, Suit.HEARTS);
        Card second = new Card(Rank.ACE, Suit.SPADES);

        assertNotEquals(first, second);
    }

    /**
     * Нельзя создать карту без достоинства.
     */
    @Test
    void nullRankThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Card(null, Suit.HEARTS));
    }

    /**
     * Нельзя создать карту без масти.
     */
    @Test
    void nullSuitThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Card(Rank.ACE, null));
    }
}