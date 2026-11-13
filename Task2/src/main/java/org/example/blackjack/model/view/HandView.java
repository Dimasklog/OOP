package org.example.blackjack.model.view;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.Money;
import org.example.blackjack.model.enums.HandStatus;

import java.util.List;

/**
 * Неизменяемый снимок руки для вывода.
 *
 * @param cards карты руки
 * @param bet ставка руки
 * @param status состояние руки
 * @param total сумма очков, включая возможный перебор
 */
public record HandView(List<Card> cards, Money bet, HandStatus status, int total) {

    /**
     * Создает снимок, копируя список карт.
     *
     * @param cards карты руки
     * @param bet ставка руки
     * @param status состояние руки
     * @param total сумма очков
     * @throws IllegalArgumentException если данные null или сумма очков отрицательная
     * @throws NullPointerException если список содержит null
     */
    public HandView {
        if (cards == null) {
            throw new IllegalArgumentException("cards must not be null");
        }
        if (bet == null) {
            throw new IllegalArgumentException("bet must not be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }

        cards = List.copyOf(cards);
    }
}