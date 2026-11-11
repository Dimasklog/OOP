package org.example.blackjack.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Рука из кард.
 */
public class Hand {
    private final List<Card> cards = new ArrayList<>();

    /**
     * Изначально оздается пустая рука.
     */
    public Hand(){
    }

    /**
     * Добавление карт.
     * @param card добавляемая карта
     * @throws IllegalArgumentException если карта null
     */
    public void addCard(Card card){
        if (card == null){
            throw new IllegalArgumentException("card must not be null");
        }
        cards.addLast(card);
    }

    /**
     * Возвращает неизменяемую копию списка карт.
     * @return неизменяемая копия карт
     */
    public List<Card> getCards() {
        return List.copyOf(cards);
    }

}
