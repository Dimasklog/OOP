package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;

/**
 * Действие добровольной остановки руки игрока.
 */
public class StandAction extends GameAction {

    /**
     * Создает действие остановки руки.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
     */
    public StandAction(PlayerId playerId, int handIndex) {
        super(playerId, handIndex);
    }

    /**
     * Останавливает выбранную руку и обновляет очередь.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если остановка этой рукой сейчас запрещена
     */
    @Override
    public void execute(BlackjackRound round) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        round.stand(getPlayerId(), getHandIndex());
    }
}