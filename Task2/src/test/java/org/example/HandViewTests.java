package org.example;

import org.example.blackjack.model.*;
import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.view.HandView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HandViewTests {
    private final Money bet = new Money(new BigDecimal("10"));

    @Test
    void cardsAreCopiedTest() {
        Card card = new Card(Rank.TEN, Suit.HEARTS);
        List<Card> cards = new ArrayList<>(List.of(card));
        HandView view = new HandView(cards, bet, HandStatus.ACTIVE, 10);

        cards.clear();

        assertEquals(List.of(card), view.cards());
        assertThrows(UnsupportedOperationException.class,
                () -> view.cards().clear());
    }

    @Test
    void snapshotDoesNotChangeTest() {
        RoundHand hand = new RoundHand(bet);
        hand.addCard(new Card(Rank.TEN, Suit.HEARTS));
        HandView view = hand.toView();

        hand.addCard(new Card(Rank.ACE, Suit.CLUBS));

        assertEquals(1, view.cards().size());
        assertEquals(10, view.total());
        assertEquals(HandStatus.ACTIVE, view.status());
        assertEquals(HandStatus.STOOD, hand.getStatus());
    }
}