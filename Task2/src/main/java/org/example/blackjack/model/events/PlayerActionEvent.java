package org.example.blackjack.model.events;

import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.enums.ActionType;

/**
 * Событие успешного действия игрока.
 *
 * @param playerId идентификатор игрока
 * @param handIndex индекс руки
 * @param actionType выполненное действие
 */
public record PlayerActionEvent(PlayerId playerId, int handIndex, ActionType actionType) implements GameEvent {

    /**
     * Создает событие выполненного действия.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @param actionType выполненное действие
     * @throws IllegalArgumentException если данные null или индекс отрицательный
     */
    public PlayerActionEvent {
        if (playerId == null) {
            throw new IllegalArgumentException("player id must not be null");
        }
        if (actionType == null) {
            throw new IllegalArgumentException("action type must not be null");
        }
        if (handIndex < 0) {
            throw new IllegalArgumentException("hand index must be non-negative");
        }
    }
}