package org.example.blackjack.model;

import org.example.blackjack.model.enums.Rank;

import java.util.List;

/**
 * Оценщик руки.
 */
public class HandEvaluator {
    /**
     * Создает оценщик руки.
     */
    public HandEvaluator(){

    }

    /**
     * Подсчет суммы очков в руке.
     * @param hand рука, которая подается
     * @return сумма очков на руке
     * @throws IllegalArgumentException если рука null
     */
    public int evaluate(Hand hand){
        if (hand == null){
            throw new IllegalArgumentException("hand must not be null");
        }
        return evaluate(hand.getCards());
    }

    private int evaluate(List<Card> cards){
        int sum = 0;
        int cntAces = 0;
        for (Card c: cards){
            sum += c.rank().getValue();
            if (c.rank().equals(Rank.ACE)){
                cntAces++;
            }
        }
        while (sum > 21 && cntAces > 0){
            sum -= 10;
            cntAces--;
        }
        return sum;
    }
}
