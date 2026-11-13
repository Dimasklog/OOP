package org.example.blackjack.model.events;

import org.example.blackjack.model.Money;
import org.example.blackjack.model.PlayerId;

import java.math.BigDecimal;

/**
 * Событие успешного принятия ставки.
 *
 * @param playerId идентификатор игрока
 * @param bet принятая ставка
 */
public record BetPlacedEvent(PlayerId playerId, Money bet)
        implements GameEvent {

    /**
     * Создает событие принятия ставки.
     *
     * @param playerId идентификатор игрока
     * @param bet принятая ставка
     * @throws IllegalArgumentException если данные null или ставка не положительная
     */
    public BetPlacedEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("player id must not be null");
        }
        if (bet == null) {
            throw new IllegalArgumentException("bet must not be null");
        }
        if (bet.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("bet must be positive");
        }
    }
}