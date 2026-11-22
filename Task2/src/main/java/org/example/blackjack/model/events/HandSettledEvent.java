package org.example.blackjack.model.events;

import org.example.blackjack.model.*;
import org.example.blackjack.model.enums.*;

import java.math.BigDecimal;

/**
 * Событие расчета результата одной руки.
 *
 * @param playerId идентификатор игрока
 * @param handIndex индекс руки
 * @param outcome результат руки
 * @param bet итоговая ставка руки
 * @param payout полный возврат денег, включая возвращаемую ставку
 */
public record HandSettledEvent(PlayerId playerId, int handIndex, HandOutcome outcome, Money bet, Money payout) implements GameEvent {

    /**
     * Создает событие расчета руки.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @param outcome результат руки
     * @param bet итоговая ставка руки
     * @param payout выплата, допускающая нулевое значение
     * @throws IllegalArgumentException если данные null, индекс отрицательный или ставка не положительная
     */
    public HandSettledEvent{
        if (playerId == null){
            throw new IllegalArgumentException("player id must not be null");
        }
        if (outcome == null){
            throw new IllegalArgumentException("outcome must not be null");
        }
        if (bet == null){
            throw new IllegalArgumentException("bet must not be null");
        }
        if (payout == null){
            throw new IllegalArgumentException("payout must not be null");
        }
        if (handIndex < 0){
            throw new IllegalArgumentException("hand index must be non-negative");
        }
        if (bet.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("bet must be positive");
        }

    }
}
