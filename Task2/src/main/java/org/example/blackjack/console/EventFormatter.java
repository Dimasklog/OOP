package org.example.blackjack.console;

import org.example.blackjack.model.enums.InsuranceStatus;
import org.example.blackjack.model.events.*;

/**
 * Преобразует события раздачи в текст.
 */
public class EventFormatter {

    /**
     * Преобразует событие в текст для вывода.
     *
     * @param event событие раздачи
     * @return описание события
     * @throws IllegalArgumentException если событие null
     */
    public String format(GameEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        return switch (event) {
            case BetPlacedEvent e ->
                    "Игрок " + e.playerId().value()
                            + " поставил " + e.bet().amount().toPlainString();

            case PhaseChangedEvent e ->
                    "Смена фазы: " + e.previousPhase()
                            + " → " + e.newPhase();

            case PlayerActionEvent e ->
                    "Игрок " + e.playerId().value()
                            + ", рука " + (e.handIndex() + 1)
                            + ": " + e.actionType();

            case HandSettledEvent e ->
                    "Игрок " + e.playerId().value()
                            + ", рука " + (e.handIndex() + 1)
                            + ": " + e.outcome()
                            + ", ставка " + e.bet().amount().toPlainString()
                            + ", выплата " + e.payout().amount().toPlainString();

            case InsuranceDecisionEvent e ->
                    e.decision().status() == InsuranceStatus.TAKEN
                            ? "Игрок " + e.playerId().value()
                            + " купил страховку на "
                            + e.decision().amount().amount().toPlainString()
                            : "Игрок " + e.playerId().value()
                            + " отказался от страховки";

            case InsuranceSettledEvent e ->
                    "Страховка игрока " + e.playerId().value()
                            + ": ставка "
                            + e.insuranceBet().amount().toPlainString()
                            + ", выплата " + e.payout().amount().toPlainString();
        };
    }
}