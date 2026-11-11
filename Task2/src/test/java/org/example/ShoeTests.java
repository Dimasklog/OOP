package org.example;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.Shoe;
import org.example.blackjack.model.enums.Rank;
import org.example.blackjack.model.enums.Suit;
import org.example.blackjack.model.exceptions.ShoeEmptyException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты башмака.
 */
class ShoeTests {

    /**
     * Количество карт зависит от количества колод.
     */
    @Test
    void deckCountTest() {
        Shoe oneDeck = new Shoe(1, new Random(42));
        Shoe sixDecks = new Shoe(6, new Random(42));

        assertEquals(52, oneDeck.remainingCards());
        assertEquals(312, sixDecks.remainingCards());
    }

    /**
     * Выдача карты уменьшает остаток на одну карту.
     */
    @Test
    void drawTest() {
        Shoe shoe = new Shoe(1, new Random(42));

        Card card = shoe.draw();

        assertNotNull(card);
        assertEquals(51, shoe.remainingCards());
    }

    /**
     * В шести колодах каждая комбинация достоинства
     * и масти встречается ровно шесть раз.
     */
    @Test
    void cardCompositionTest() {
        Shoe shoe = new Shoe(6, new Random(42));
        List<Card> drawnCards = new ArrayList<>();

        while (shoe.remainingCards() > 0) {
            drawnCards.add(shoe.draw());
        }

        assertEquals(312, drawnCards.size());

        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                Card expectedCard = new Card(rank, suit);

                assertEquals(
                        6,
                        Collections.frequency(drawnCards, expectedCard)
                );
            }
        }
    }

    /**
     * Нельзя создать башмак из нуля колод.
     */
    @Test
    void zeroDeckCountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shoe(0, new Random(42)));
    }

    /**
     * Нельзя создать башмак с отрицательным количеством колод.
     */
    @Test
    void negativeDeckCountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shoe(-1, new Random(42)));
    }

    /**
     * Генератор случайных чисел обязателен.
     */
    @Test
    void nullRandomThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Shoe(1, null));
    }

    /**
     * После выдачи всех карт башмак выбрасывает исключение.
     */
    @Test
    void emptyShoeThrows() {
        Shoe shoe = new Shoe(1, new Random(42));

        for (int i = 0; i < 52; i++) {
            shoe.draw();
        }

        assertEquals(0, shoe.remainingCards());

        assertThrows(ShoeEmptyException.class,
                () -> shoe.draw());

        assertThrows(ShoeEmptyException.class,
                () -> shoe.draw());

        assertEquals(0, shoe.remainingCards());
    }

    /**
     * Одинаковое зерно даёт одинаковый порядок всех карт.
     */
    @Test
    void sameSeedTest() {
        Shoe first = new Shoe(6, new Random(42));
        Shoe second = new Shoe(6, new Random(42));

        for (int i = 0; i < 312; i++) {
            assertEquals(first.draw(), second.draw());
        }

        assertEquals(0, first.remainingCards());
        assertEquals(0, second.remainingCards());
    }
}