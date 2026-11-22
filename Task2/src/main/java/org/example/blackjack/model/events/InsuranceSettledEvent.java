package org.example.blackjack.model.events;

import org.example.blackjack.model.*;

import java.math.BigDecimal;

/**
 * Результат расчета страховой ставки.
 *
 * @param playerId идентификатор игрока
 * @param insuranceBet страховая ставка
 * @param payout выплата с учетом возврата страховой ставки
 */

public record InsuranceSettledEvent(PlayerId playerId, Money insuranceBet, Money payout) implements GameEvent{
    /**
     * Создает событие расчета страховки.
     *
     * @param playerId идентификатор игрока
     * @param insuranceBet страховая ставка
     * @param payout выплата, включая ноль при проигрыше
     * @throws IllegalArgumentException если данные null или страховая ставка не больше нуля
     */
    public InsuranceSettledEvent{
        if (playerId == null){
            throw new IllegalArgumentException("player id must not be null");
        }
        if (insuranceBet == null){
            throw new IllegalArgumentException("insurance bet must not be null");
        }
        if (payout == null){
            throw new IllegalArgumentException("payout must not be null");
        }
        if (insuranceBet.amount().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("insurance bet must be positive");
        }
    }
}
