package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;

/**
 * Действие сдачи руки с возвратом половины ставки при расчете.
 */
public class SurrenderAction extends GameAction {

    /**
     * Создает действие сдачи руки.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null
     *                                  или индекс отрицательный
     */
    public SurrenderAction(PlayerId playerId, int handIndex) {
        super(playerId, handIndex);
    }

    /**
     * Сдает выбранную руку и обновляет очередь.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если сдача этой рукой сейчас запрещена
     */
    @Override
    public void execute(BlackjackRound round) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        round.surrender(getPlayerId(), getHandIndex());
    }
}