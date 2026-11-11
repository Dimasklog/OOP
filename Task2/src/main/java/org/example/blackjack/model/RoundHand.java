package org.example.blackjack.model;

import org.example.blackjack.model.enums.HandStatus;
import org.example.blackjack.model.exceptions.IllegalMoveException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Рука в раздаче со своей ставкой и состоянием.
 */
public class RoundHand {
    private final Hand hand = new Hand();
    private Money bet;
    private HandStatus status;
    private final HandEvaluator evaluator = new HandEvaluator();

    /**
     * Создает пустую активную руку с указанной ставкой.
     *
     * @param bet ставка руки
     * @throws IllegalArgumentException если ставка null или не больше нуля
     */
    public RoundHand(Money bet) {
        if (bet == null) {
            throw new IllegalArgumentException("bet must not be null");
        }
        if (bet.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("bet must be positive");
        }

        this.bet = bet;
        this.status = HandStatus.ACTIVE;
    }

    /**
     * Возвращает общую сумму очков руки.
     *
     * @return общая сумма очков
     */
    public int getTotal(){
        return evaluator.evaluate(hand);
    }

    /**
     * Добавляет карту в руку, при 21 останавливается, а при превышении 21 переходит в состояние перебора.
     *
     * @param card добавляемая карта
     * @throws IllegalArgumentException если {@code card == null}
     * @throws IllegalMoveException если {@code status.isFinished()}
     */
    public void addCard(Card card){
        if (card == null){
            throw new IllegalArgumentException("card must not be null");
        }
        if (status.isFinished()){
            throw new IllegalMoveException("status is finished, card must not be added");
        }
        hand.addCard(card);
        int newSum = getTotal();
        if (newSum > 21){
            status = HandStatus.BUST;
        } else if (newSum == 21) {
            status = HandStatus.STOOD;
        }
    }

    /**
     * Добровольная остановка.
     * @throws IllegalMoveException если {@code status.isFinished()} или {@code hand.getCards().size() < 2}
     */
    public void stand(){
        if (status.isFinished()){
            throw new IllegalMoveException("status must not be finished");
        }
        if (hand.getCards().size() < 2){
            throw new IllegalMoveException("initial distribution is not yet completed");
        }
        status = HandStatus.STOOD;
    }

    /**
     * Возвращает неизменяемую копию списка карт.
     * Последующие изменения руки не меняют полученную копию.
     *
     * @return неизменяемая копия карт
     */
    public List<Card> getCards() {
        return hand.getCards();
    }

    /**
     * Возвращает ставку руки.
     *
     * @return ставка руки
     */
    public Money getBet() {
        return bet;
    }

    /**
     * Возвращает текущее состояние руки.
     *
     * @return состояние руки
     */
    public HandStatus getStatus() {
        return status;
    }
}