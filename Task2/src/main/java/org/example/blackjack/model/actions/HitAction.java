package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;
import org.example.blackjack.model.exceptions.ShoeEmptyException;

/**
 * Действие добора одной карты в руку игрока.
 */
public class HitAction extends GameAction {

    /**
     * Создает действие добора карты.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null или индекс отрицательный
     */
    public HitAction(PlayerId playerId, int handIndex) {
        super(playerId, handIndex);
    }

    /**
     * Добавляет карту в выбранную руку и обновляет очередь.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если добор этой рукой сейчас запрещен
     * @throws ShoeEmptyException если в башмаке закончились карты
     */
    @Override
    public void execute(BlackjackRound round) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        round.hit(getPlayerId(), getHandIndex());
    }
}