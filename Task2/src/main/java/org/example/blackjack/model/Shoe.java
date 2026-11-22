package org.example.blackjack.model;

import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.exceptions.ShoeEmptyException;

import java.util.*;

/**
 * Башмак.
 */
public class Shoe {

    private final List<Card> cards = new ArrayList<>();
    private final int deckCount;

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

        this.deckCount = deckCount;
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

    /**
     * Подбирает башмак с достаточным запасом карт для новой раздачи.
     * Сохраняет текущий башмак, если карт хватает; иначе создает новый.
     *
     * @param current текущий башмак или null перед первой раздачей
     * @param requiredCards требуемое количество карт
     * @param minimumDeckCount минимальное количество колод в новом башмаке
     * @param random общий генератор игры
     * @return текущий или новый башмак
     * @throws IllegalArgumentException если числовые параметры недопустимы
     *                                  или генератор null
     */
    public static Shoe prepareForRound(
            Shoe current, int requiredCards,
            int minimumDeckCount, Random random) {

        if (requiredCards <= 0 || minimumDeckCount <= 0
                || minimumDeckCount > Integer.MAX_VALUE / 52
                || random == null) {
            throw new IllegalArgumentException(
                    "invalid shoe preparation arguments"
            );
        }

        if (current != null && current.remainingCards() >= requiredCards) {
            return current;
        }

        long neededDecks = (requiredCards + 51L) / 52;

        if (neededDecks > Integer.MAX_VALUE / 52) {
            throw new IllegalArgumentException(
                    "required card count is too large"
            );
        }

        int count = Math.max(minimumDeckCount, (int) neededDecks);
        return new Shoe(count, random);
    }

    /**
     * Возвращает количество колод, из которых создан башмак.
     *
     * @return количество колод
     */
    public int getDeckCount() {
        return deckCount;
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
