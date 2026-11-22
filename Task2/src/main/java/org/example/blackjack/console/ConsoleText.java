package org.example.blackjack.console;

import org.example.blackjack.model.Card;
import org.example.blackjack.model.enums.*;

import java.util.List;
import java.util.StringJoiner;

/**
 * Русские названия для консольного вывода.
 */
public final class ConsoleText {

    private ConsoleText() {
    }

    /**
     * Форматирует список карт.
     *
     * @param cards карты
     * @return русские названия карт в квадратных скобках
     */
    public static String cards(List<Card> cards) {
        StringJoiner result = new StringJoiner(", ", "[", "]");

        for (Card card : cards) {
            result.add(rank(card.rank()) + " " + suit(card.suit()));
        }

        return result.toString();
    }

    /**
     * Возвращает название действия.
     *
     * @param action действие
     * @return русское название
     */
    public static String action(ActionType action) {
        return switch (action) {
            case HIT -> "взять карту";
            case STAND -> "остановиться";
            case DOUBLE -> "удвоить ставку";
            case SPLIT -> "разделить пару";
            case SURRENDER -> "сдаться";
        };
    }

    /**
     * Возвращает название фазы.
     *
     * @param phase фаза раздачи
     * @return русское название
     */
    public static String phase(RoundPhase phase) {
        return switch (phase) {
            case BETTING -> "прием ставок";
            case INITIAL_DEAL -> "начальная раздача";
            case INSURANCE -> "страховка";
            case PLAYER_TURNS -> "ходы игроков";
            case DEALER_TURN -> "ход дилера";
            case SETTLEMENT -> "расчет выплат";
            case FINISHED -> "раздача завершена";
        };
    }

    /**
     * Возвращает название состояния руки.
     *
     * @param status состояние руки
     * @return русское название
     */
    public static String status(HandStatus status) {
        return switch (status) {
            case ACTIVE -> "активна";
            case STOOD -> "остановлена";
            case BUST -> "перебор";
            case SURRENDERED -> "сдана";
        };
    }

    /**
     * Возвращает название результата руки.
     *
     * @param outcome результат руки
     * @return русское название
     */
    public static String outcome(HandOutcome outcome) {
        return switch (outcome) {
            case LOSS -> "проигрыш";
            case PUSH -> "ничья";
            case WIN -> "выигрыш";
            case BLACKJACK -> "блэкджек";
            case SURRENDER -> "сдача";
        };
    }

    private static String rank(Rank rank) {
        return switch (rank) {
            case TWO -> "Двойка";
            case THREE -> "Тройка";
            case FOUR -> "Четверка";
            case FIVE -> "Пятерка";
            case SIX -> "Шестерка";
            case SEVEN -> "Семерка";
            case EIGHT -> "Восьмерка";
            case NINE -> "Девятка";
            case TEN -> "Десятка";
            case JACK -> "Валет";
            case QUEEN -> "Дама";
            case KING -> "Король";
            case ACE -> "Туз";
        };
    }

    private static String suit(Suit suit) {
        return switch (suit) {
            case SPADES -> "пик";
            case HEARTS -> "червей";
            case DIAMONDS -> "бубен";
            case CLUBS -> "треф";
        };
    }
}