package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;
import org.example.blackjack.model.exceptions.InsufficientFundsException;
import org.example.blackjack.model.exceptions.ShoeEmptyException;

/**
 * Действие удвоения ставки с получением одной последней карты.
 */
public class DoubleAction extends GameAction {

    /**
     * Создает действие удвоения.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
     */
    public DoubleAction(PlayerId playerId, int handIndex) {
        super(playerId, handIndex);
    }

    /**
     * Удваивает ставку выбранной руки и завершает ее действия.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если удвоение сейчас запрещено
     * @throws InsufficientFundsException если не хватает денег
     * @throws ShoeEmptyException если башмак пуст
     */
    @Override
    public void execute(BlackjackRound round) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        round.doubleDown(getPlayerId(), getHandIndex());
    }
}