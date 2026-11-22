package org.example.blackjack.console;

import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.enums.InsuranceStatus;
import org.example.blackjack.model.events.*;

import java.util.Map;

/**
 * Преобразует события раздачи в текст.
 */
public class EventFormatter {
    private final Map<PlayerId, String> playerNames;

    /**
     * Создает форматтер с именами игроков.
     *
     * @param playerNames имена игроков по идентификаторам
     * @throws IllegalArgumentException если словарь null
     * @throws NullPointerException если словарь содержит null
     */
    public EventFormatter(Map<PlayerId, String> playerNames) {
        if (playerNames == null) {
            throw new IllegalArgumentException("player names must not be null");
        }
        this.playerNames = Map.copyOf(playerNames);
    }

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
                    formatPlayer(e.playerId())
                            + " поставил " + e.bet().amount().toPlainString();

            case PhaseChangedEvent e ->
                    "Смена фазы: " + ConsoleText.phase(e.previousPhase())
                            + " → " + ConsoleText.phase(e.newPhase());

            case PlayerActionEvent e ->
                    formatPlayer(e.playerId())
                            + ", рука " + (e.handIndex() + 1)
                            + ": " + ConsoleText.action(e.actionType());

            case HandSettledEvent e ->
                    formatPlayer(e.playerId())
                            + ", рука " + (e.handIndex() + 1)
                            + ": " + ConsoleText.outcome(e.outcome())
                            + ", ставка " + e.bet().amount().toPlainString()
                            + ", выплата " + e.payout().amount().toPlainString();

            case InsuranceDecisionEvent e ->
                    e.decision().status() == InsuranceStatus.TAKEN
                            ? formatPlayer(e.playerId())
                            + " купил страховку на "
                            + e.decision().amount().amount().toPlainString()
                            : formatPlayer(e.playerId())
                            + " отказался от страховки";

            case InsuranceSettledEvent e ->
                    "Страховка игрока " + formatPlayer(e.playerId())
                            + ": ставка "
                            + e.insuranceBet().amount().toPlainString()
                            + ", выплата "
                            + e.payout().amount().toPlainString();
        };
    }

    private String formatPlayer(PlayerId playerId) {
        return playerNames.getOrDefault(playerId, "Игрок")
                + " (ID: " + playerId.value() + ")";
    }
}