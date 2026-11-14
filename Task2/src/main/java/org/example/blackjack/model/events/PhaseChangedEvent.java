package org.example.blackjack.model.events;

import org.example.blackjack.model.enums.RoundPhase;

/**
 * Событие перехода раздачи в другую фазу.
 *
 * @param previousPhase предыдущая фаза
 * @param newPhase новая фаза
 */
public record PhaseChangedEvent(RoundPhase previousPhase, RoundPhase newPhase) implements GameEvent {

    /**
     * Создает событие смены фазы.
     *
     * @param previousPhase предыдущая фаза
     * @param newPhase новая фаза
     * @throws IllegalArgumentException если фазы null или совпадают
     */
    public PhaseChangedEvent{
        if (previousPhase == null){
            throw new IllegalArgumentException("previous phase must not be null");
        }
        if (newPhase == null){
            throw new IllegalArgumentException("new phase must not be null");
        }
        if (previousPhase == newPhase){
            throw new IllegalArgumentException("previous phase and new phase must not be the same");
        }
    }
}
