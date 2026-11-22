package org.example.blackjack.console;

import org.example.blackjack.model.*;
import org.example.blackjack.model.actions.*;
import org.example.blackjack.model.enums.RoundPhase;
import org.example.blackjack.model.exceptions.*;
import org.example.blackjack.model.view.HandView;

import java.math.BigDecimal;
import java.util.*;

/**
 * Консольное приложение для блэкджека.
 */
public class BlackjackApplication {
    private static final Money BET = new Money(new BigDecimal("10"));
    private static final int DECK_COUNT = 6;

    /**
     * Запускает игру с сохранением балансов между раздачами.
     *
     * @param args аргументы командной строки;
     *             первый аргумент — необязательное зерно генератора
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            long seed = args.length > 0
                    ? Long.parseLong(args[0])
                    : System.currentTimeMillis();

            System.out.println("Зерно игры: " + seed);

            Random random = new Random(seed);
            Shoe shoe = null;

            Player firstPlayer = new Player(
                    new PlayerId(100),
                    "Вася",
                    new Money(new BigDecimal("100"))
            );

            Player secondPlayer = new Player(
                    new PlayerId(200),
                    "Петя",
                    new Money(new BigDecimal("100"))
            );

            List<Player> players = List.of(firstPlayer, secondPlayer);
            int roundNumber = 1;

            while (true) {
                List<Player> participants = new ArrayList<>();

                for (Player player : players) {
                    if (player.getBalance().amount()
                            .compareTo(BET.amount()) >= 0) {
                        participants.add(player);
                    } else {
                        System.out.println(
                                player.getName()
                                        + " пропускает раздачу: "
                                        + "недостаточно денег для ставки."
                        );
                    }
                }

                if (participants.isEmpty()) {
                    System.out.println(
                            "Ни у одного игрока не хватает денег на ставку."
                    );
                    break;
                }

                int cardReserve =
                        BlackjackRound.requiredCardReserve(participants.size());

                Shoe prepared = Shoe.prepareForRound(
                        shoe, cardReserve, DECK_COUNT, random
                );

                if (prepared != shoe) {
                    shoe = prepared;
                    System.out.println(
                            "Подготовлен новый башмак: "
                                    + shoe.getDeckCount() + " колод."
                    );
                }

                System.out.println();
                System.out.println("=== Раздача " + roundNumber + " ===");

                playRound(scanner, participants, shoe);
                roundNumber++;

                System.out.print(
                        "Еще раздача? 1 — да, другое — выход: "
                );

                if (!scanner.nextLine().trim().equals("1")) {
                    break;
                }
            }

            printBalances(players);
            System.out.println("Игра завершена.");
        } catch (ShoeEmptyException e) {
            System.out.println(
                    "Игра прервана: во время раздачи закончились карты."
            );
        } catch (NumberFormatException e) {
            System.out.println("Зерно должно быть целым числом типа long.");
        } catch (NoSuchElementException e) {
            System.out.println("Ввод завершен. Приложение закрыто.");
        }
    }

    private static void playRound(
            Scanner scanner, List<Player> players, Shoe shoe) {

        Map<PlayerId, String> playerNames = new LinkedHashMap<>();

        for (Player player : players) {
            playerNames.put(player.getId(), player.getName());
        }

        BlackjackRound round = new BlackjackRound(players, shoe);
        ConsoleJournal journal = new ConsoleJournal(round, playerNames);

        for (Player player : players) {
            round.placeBet(player.getId(), BET);
        }

        round.start();
        journal.printNewEvents();
        printTable(round, players);

        while (round.getPhase() != RoundPhase.FINISHED) {
            try {
                switch (round.getPhase()) {
                    case INSURANCE ->
                            readInsurance(scanner, round, players);

                    case PLAYER_TURNS -> {
                        PlayerId playerId = round.getCurrentPlayerId();
                        int handIndex = round.getCurrentHandIndex();

                        System.out.println(
                                "Ходит " + playerNames.get(playerId)
                                        + " (ID: " + playerId.value() + ")"
                                        + ", рука " + (handIndex + 1)
                        );

                        GameAction action = readAction(
                                scanner, playerId, handIndex
                        );

                        action.execute(round);
                    }

                    case DEALER_TURN -> {
                        System.out.println("Ход дилера.");
                        round.playDealer();
                    }

                    case SETTLEMENT -> round.settle();

                    default -> throw new IllegalStateException(
                            "Неожиданная фаза: " + round.getPhase()
                    );
                }
            } catch (IllegalMoveException e) {
                System.out.println(
                        "Действие отклонено: " + e.getMessage()
                );
                continue;
            } catch (InsufficientFundsException e) {
                System.out.println("Недостаточно денег для этого действия.");
                continue;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                continue;
            }

            journal.printNewEvents();
            printTable(round, players);
        }

        System.out.println("Раздача завершена.");
    }

    private static GameAction readAction(
            Scanner scanner, PlayerId playerId, int handIndex) {

        System.out.println("1 — взять карту");
        System.out.println("2 — остановиться");
        System.out.println("3 — удвоить ставку");
        System.out.println("4 — разделить пару");
        System.out.println("5 — сдаться");
        System.out.print("Выберите действие: ");

        String command = scanner.nextLine().trim();

        return switch (command) {
            case "1" -> new HitAction(playerId, handIndex);
            case "2" -> new StandAction(playerId, handIndex);
            case "3" -> new DoubleAction(playerId, handIndex);
            case "4" -> new SplitAction(playerId, handIndex);
            case "5" -> new SurrenderAction(playerId, handIndex);
            default -> throw new IllegalArgumentException(
                    "Введите число от 1 до 5"
            );
        };
    }

    private static void readInsurance(
            Scanner scanner, BlackjackRound round, List<Player> players) {

        for (Player player : players) {
            Money mainBet = round.getHandViews(player.getId())
                    .getFirst().bet();

            Money maximum = mainBet.multiply(new BigDecimal("0.5"));
            boolean decided = false;

            while (!decided) {
                System.out.println();
                System.out.println(
                        player.getName()
                                + " (ID: " + player.getId().value() + ")"
                                + ", дилер предлагает страховку."
                );
                System.out.println(
                        "Максимальная сумма: "
                                + maximum.amount().toPlainString()
                );
                System.out.print("Введите сумму или 0 для отказа: ");

                String input = scanner.nextLine().trim();

                try {
                    Money amount = new Money(
                            new BigDecimal(input.replace(',', '.'))
                    );

                    if (amount.amount().compareTo(BigDecimal.ZERO) == 0) {
                        round.declineInsurance(player.getId());
                    } else {
                        round.buyInsurance(player.getId(), amount);
                    }

                    decided = true;
                } catch (IllegalMoveException e) {
                    System.out.println(
                            "Решение отклонено: " + e.getMessage()
                    );
                } catch (InsufficientFundsException e) {
                    System.out.println(
                            "Недостаточно денег для покупки страховки."
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println(
                            "Введите допустимую сумму страховки или 0."
                    );
                }
            }
        }
    }

    private static void printTable(
            BlackjackRound round, List<Player> players) {

        System.out.println();
        System.out.println(
                "Дилер: " + ConsoleText.cards(round.getVisibleDealerCards())
        );

        if (round.isDealerRevealed()) {
            System.out.println("Очки дилера: " + round.getDealerTotal());
        } else {
            System.out.println("Вторая карта закрыта");
        }

        for (Player player : players) {
            System.out.println();
            System.out.println(
                    player.getName()
                            + " (ID: " + player.getId().value() + ")"
                            + ", баланс: "
                            + player.getBalance().amount().toPlainString()
            );

            List<HandView> hands = round.getHandViews(player.getId());

            for (int i = 0; i < hands.size(); i++) {
                HandView hand = hands.get(i);

                System.out.println(
                        "Рука " + (i + 1)
                                + ": " + ConsoleText.cards(hand.cards())
                                + ", очки: " + hand.total()
                                + ", ставка: "
                                + hand.bet().amount().toPlainString()
                                + ", состояние: "
                                + ConsoleText.status(hand.status())
                );
            }
        }

        System.out.println();
    }

    private static void printBalances(List<Player> players) {
        List<Player> sortedPlayers = new ArrayList<>(players);

        sortedPlayers.sort(
                Comparator.comparing(
                                (Player player) -> player.getBalance().amount()
                        )
                        .reversed()
                        .thenComparingLong(player -> player.getId().value())
        );

        System.out.println();
        System.out.println("Итоговые балансы по убыванию:");

        for (Player player : sortedPlayers) {
            System.out.println(
                    player.getName()
                            + " (ID: " + player.getId().value() + ")"
                            + ": "
                            + player.getBalance().amount().toPlainString()
            );
        }
    }
}