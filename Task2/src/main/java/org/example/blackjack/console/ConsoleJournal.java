package org.example.blackjack.console;

import org.example.blackjack.model.BlackjackRound;
import org.example.blackjack.model.PlayerId;
import org.example.blackjack.model.events.GameEvent;

import java.util.List;
import java.util.Map;

/**
 * Выводит новые события одной раздачи в консоль.
 */
public class ConsoleJournal {
    private final BlackjackRound round;
    private final EventFormatter formatter;
    private int printedEventCount = 0;

    /**
     * Создает журнал для раздачи.
     *
     * @param round раздача
     * @param playerNames имена игроков по идентификаторам
     * @throws IllegalArgumentException если раздача или словарь null
     * @throws NullPointerException если словарь содержит null
     */
    public ConsoleJournal(BlackjackRound round,
                          Map<PlayerId, String> playerNames) {
        if (round == null) {
            throw new IllegalArgumentException("round must not be null");
        }
        this.round = round;
        this.formatter = new EventFormatter(playerNames);
    }

    /**
     * Выводит события, которые еще не были напечатаны.
     */
    public void printNewEvents(){
        List<GameEvent> events = round.getEvents();
        while (printedEventCount < events.size()){
            String string = formatter.format(events.get(printedEventCount));
            System.out.println(string);
            printedEventCount++;
        }
    }
}
