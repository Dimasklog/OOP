package org.example.blackjack.model.events;

import org.example.blackjack.model.*;

/**
 * Решение игрока о покупке страховки или отказе от нее.
 *
 * @param playerId идентификатор игрока
 * @param decision принятое решение
 */

public record InsuranceDecisionEvent(PlayerId playerId, InsuranceDecision decision) implements GameEvent{
    /**
     * Создает событие принятого решения о страховке.
     *
     * @param playerId идентификатор игрока
     * @param decision принятое решение
     * @throws IllegalArgumentException если идентификатор или решение null, либо решение еще не принято
     */
    public InsuranceDecisionEvent{
        if (playerId == null){
            throw new IllegalArgumentException("player id must not be null");
        }
        if (decision == null){
            throw new IllegalArgumentException("decision must not be null");
        }
        if (!decision.status().isDecided()){
            throw new IllegalArgumentException("decision status must be decided");
        }
    }

}
