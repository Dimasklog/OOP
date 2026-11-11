package org.example.blackjack.model;

import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.exceptions.ShoeEmptyException;

import java.util.*;

/**
 * Башмак.
 */
public class Shoe {

    private final List<Card> cards = new ArrayList<>();

    /**
     * Создает башмак.
     * @param deckCount количество колод
     * @param random генератор случайных чисел для перемешивания
     * @throws IllegalArgumentException если {@code deckCount <= 0} или {@code random == null}
     */
    public Shoe(int deckCount, Random random){
        if (deckCount <= 0){
            throw new IllegalArgumentException("deck count must be > 0");
        }
        if (random == null){
            throw new IllegalArgumentException("random must not be null");
        }

        fillCards(deckCount);
        Collections.shuffle(cards, random);
    }

    /**
     * Остаток карт.
     * @return количество оставшихся карт
     */
    public int remainingCards(){
        return cards.size();
    }

    /**
     * Выдает карту.
     * @return карта
     * @throws ShoeEmptyException если башмак пустой
     */
    public Card draw(){
        if (cards.isEmpty()){
            throw new ShoeEmptyException("shoe is empty");
        }
        return cards.removeLast();
    }

    private void fillCards(int deckCount){
        for (int i = 0; i < deckCount; i++){
            for (Rank rank: Rank.values()){
                for (Suit suit: Suit.values()){
                    cards.addLast(new Card(rank, suit));
                }
            }
        }
    }
}
