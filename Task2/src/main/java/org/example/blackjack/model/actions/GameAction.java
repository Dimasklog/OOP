package org.example.blackjack.model.actions;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.exceptions.IllegalMoveException;

/**
 * Базовый класс игрового действия над рукой игрока.
 */
public abstract class GameAction {
    private final PlayerId playerId;
    private final int handIndex;

    /**
     * Создает действие для указанного игрока и руки.
     *
     * @param playerId идентификатор игрока
     * @param handIndex индекс руки, начиная с нуля
     * @throws IllegalArgumentException если идентификатор null
     *                                  или индекс отрицательный
     */
    protected GameAction(PlayerId playerId, int handIndex) {
        if (playerId == null) {
            throw new IllegalArgumentException("player id must be not null");
        }
        if (handIndex < 0) {
            throw new IllegalArgumentException("hand index must be non-negative");
        }
        this.playerId = playerId;
        this.handIndex = handIndex;
    }

    /**
     * Возвращает идентификатор игрока.
     *
     * @return идентификатор игрока
     */
    public PlayerId getPlayerId() {
        return playerId;
    }

    /**
     * Возвращает индекс руки.
     *
     * @return индекс руки, начиная с нуля
     */
    public int getHandIndex() {
        return handIndex;
    }

    /**
     * Выполняет действие в указанной раздаче.
     *
     * @param round раздача, в которой выполняется действие
     * @throws IllegalArgumentException если раздача null
     * @throws IllegalMoveException если действие сейчас запрещено
     */
    public abstract void execute(BlackjackRound round);
}