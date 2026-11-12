package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;
import org.example.blackjack.model.exceptions.InsufficientFundsException;

/**
 * Действие разделения пары на две отдельные руки.
 */
public class SplitAction extends GameAction {

    /**
     * Создает действие разделения пары.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
     */
    public SplitAction(PlayerId playerId, int handIndex) {
        super(playerId, handIndex);
    }

    /**
     * Разделяет пару и обновляет очередь.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если разделение сейчас запрещено
     * @throws InsufficientFundsException если не хватает денег
     */
    @Override
    public void execute(BlackjackRound round) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        round.split(getPlayerId(), getHandIndex());
    }
}