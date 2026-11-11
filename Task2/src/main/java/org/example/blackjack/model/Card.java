package org.example.blackjack.model;

import org.example.blackjack.model.enums.*;

/**
 * Запись карты.
 * @param rank имя и значение карты
 * @param suit масть карты
 */
public record Card(Rank rank, Suit suit) {
    /**
     * Создает карту.
     * @param rank имя и значение карты
     * @param suit масть карты
     * @throws IllegalArgumentException если хотя бы одно значение аргументов null
     */
    public Card{
        if (rank == null){
            throw new IllegalArgumentException("rank must not be null");
        }
        if (suit == null){
            throw new IllegalArgumentException("suit must not be null");
        }

    }
}
