package org.example;

import org.example.blackjack.model.*;
import org.example.blackjack.model.actions.*;
import org.example.blackjack.model.events.*;
import org.example.blackjack.model.enums.*;
import org.example.blackjack.model.exceptions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты раздачи.
 */
class BlackjackRoundTests {
    private final PlayerId id = new PlayerId(1);
    private final Money bet = money("10");
    private final Player player = new Player(id, "Иван", money("100"));
    private final Shoe shoe = new Shoe(1, new Random(0));
    private final BlackjackRound round =
            new BlackjackRound(List.of(player), shoe);

    @Test
    void initialStateTest() {
        assertEquals(RoundPhase.BETTING, round.getPhase());
        assertEquals(List.of(id), round.getPlayerIds());
        assertThrows(UnsupportedOperationException.class,
                () -> round.getPlayerIds().clear());
        assertThrows(IllegalMoveException.class,
                () -> round.getCurrentPlayerId());
        assertThrows(IllegalMoveException.class,
                () -> round.getCurrentHandIndex());
        assertThrows(IllegalMoveException.class, () -> round.playDealer());
    }

    @Test
    void invalidConstructorTest() {
        List<Player> withNull = new ArrayList<>();
        withNull.add(null);
        Player duplicate = new Player(
                new PlayerId(1), "Анна", money("100")
        );

        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackRound(null, shoe));
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackRound(List.of(), shoe));
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackRound(List.of(player), null));
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackRound(withNull, shoe));
        assertThrows(IllegalArgumentException.class,
                () -> new BlackjackRound(List.of(player, duplicate), shoe));
    }

    @Test
    void placeBetTest() {
        round.placeBet(id, bet);

        assertEquals(money("90"), player.getBalance());
        assertEquals(52, shoe.remainingCards());
        assertEquals(RoundPhase.BETTING, round.getPhase());

        assertThrows(IllegalMoveException.class,
                () -> round.placeBet(id, bet));
        assertEquals(money("90"), player.getBalance());
    }

    @Test
    void invalidBetTest() {
        assertThrows(IllegalArgumentException.class,
                () -> round.placeBet(null, bet));
        assertThrows(IllegalArgumentException.class,
                () -> round.placeBet(id, null));
        assertThrows(IllegalArgumentException.class,
                () -> round.placeBet(id, money("0")));
        assertThrows(IllegalMoveException.class,
                () -> round.placeBet(new PlayerId(2), bet));

        assertEquals(money("100"), player.getBalance());
        round.placeBet(id, bet);
        assertEquals(money("90"), player.getBalance());
    }

    @Test
    void insufficientFundsTest() {
        assertThrows(InsufficientFundsException.class,
                () -> round.placeBet(id, money("101")));
        assertEquals(money("100"), player.getBalance());

        round.placeBet(id, bet);
        assertEquals(money("90"), player.getBalance());
    }

    @Test
    void startWithoutBetTest() {
        assertThrows(IllegalMoveException.class, () -> round.start());

        assertEquals(RoundPhase.BETTING, round.getPhase());
        assertEquals(52, shoe.remainingCards());
    }

    @Test
    void startTest() {
        round.placeBet(id, bet);
        round.start();

        assertEquals(RoundPhase.PLAYER_TURNS, round.getPhase());
        assertEquals(id, round.getCurrentPlayerId());
        assertEquals(0, round.getCurrentHandIndex());

        assertThrows(IllegalMoveException.class, () -> round.start());
        assertThrows(IllegalMoveException.class,
                () -> round.placeBet(id, bet));

        assertEquals(48, shoe.remainingCards());
        assertEquals(money("90"), player.getBalance());
    }

    @Test
    void phasesTest() {
        assertEquals(RoundPhase.INSURANCE, startWithSeed(8).getPhase());
        assertEquals(RoundPhase.SETTLEMENT, startWithSeed(49).getPhase());
        assertEquals(RoundPhase.SETTLEMENT, startWithSeed(26).getPhase());
    }

    @Test
    void skipBlackjackTest() {
        Player second = new Player(new PlayerId(2), "Иван", money("100"));
        BlackjackRound game = new BlackjackRound(
                List.of(second, player), new Shoe(1, new Random(20))
        );
        game.placeBet(second.getId(), bet);
        game.placeBet(id, bet);
        game.start();

        assertEquals(List.of(second.getId(), id), game.getPlayerIds());
        assertEquals(RoundPhase.PLAYER_TURNS, game.getPhase());
        assertEquals(id, game.getCurrentPlayerId());
        assertEquals(0, game.getCurrentHandIndex());
    }

    @Test
    void insufficientCardsTest() {
        for (int i = 0; i < 49; i++) {
            shoe.draw();
        }
        round.placeBet(id, bet);

        assertThrows(IllegalMoveException.class, () -> round.start());

        assertEquals(RoundPhase.BETTING, round.getPhase());
        assertEquals(3, shoe.remainingCards());
        assertEquals(money("90"), player.getBalance());
    }

    private Money money(String value) {
        return new Money(new BigDecimal(value));
    }

    private BlackjackRound startWithSeed(int seed) {
        Player participant = new Player(id, "Иван", money("100"));
        BlackjackRound game = new BlackjackRound(
                List.of(participant), new Shoe(1, new Random(seed))
        );
        game.placeBet(id, bet);
        game.start();
        return game;
    }
    @Test
    void standActionTest() {
        round.placeBet(id, bet);
        round.start();

        GameAction action = new StandAction(new PlayerId(1), 0);
        action.execute(round);

        assertEquals(RoundPhase.DEALER_TURN, round.getPhase());
    }

    @Test
    void hitActionTest() {
        round.placeBet(id, bet);
        round.start();

        GameAction action = new HitAction(new PlayerId(1), 0);
        action.execute(round);

        // При нашем seed 0 игрок получает перебор после добора.
        assertEquals(47, shoe.remainingCards());
        assertEquals(RoundPhase.DEALER_TURN, round.getPhase());
    }

    @Test
    void actionsRejectNullRoundTest() {
        GameAction hit = new HitAction(id, 0);
        GameAction stand = new StandAction(id, 0);

        assertThrows(IllegalArgumentException.class,
                () -> hit.execute(null));
        assertThrows(IllegalArgumentException.class,
                () -> stand.execute(null));
    }
    @Test
    void dealerDrawsTest() {
        round.placeBet(id, bet);
        round.start();
        round.stand(id, 0);

        round.playDealer();

        assertEquals(RoundPhase.SETTLEMENT, round.getPhase());
        assertEquals(46, shoe.remainingCards());
    }

    @Test
    void dealerSkipsBustedHandsTest() {
        round.placeBet(id, bet);
        round.start();
        round.hit(id, 0); // При seed 0 игрок перебирает.

        round.playDealer();

        assertEquals(RoundPhase.SETTLEMENT, round.getPhase());
        assertEquals(47, shoe.remainingCards());
    }

    @Test
    void dealerStopsOnSoft17Test() {
        // При seed 242 дилер получает шестёрку и туза.
        Shoe softShoe = new Shoe(1, new Random(242));
        BlackjackRound game = new BlackjackRound(List.of(player), softShoe);
        game.placeBet(id, bet);
        game.start();
        game.stand(id, 0);

        game.playDealer();

        assertEquals(RoundPhase.SETTLEMENT, game.getPhase());
        assertEquals(48, softShoe.remainingCards());
    }
    @Test
    void winningRoundTest() {
        round.placeBet(id, bet);
        round.start();
        round.stand(id, 0);
        round.playDealer();
        round.settle();

        assertEquals(RoundPhase.FINISHED, round.getPhase());
        assertEquals(money("110"), player.getBalance());

        assertThrows(IllegalMoveException.class, () -> round.settle());
        assertEquals(money("110"), player.getBalance());
    }

    @Test
    void losingRoundTest() {
        round.placeBet(id, bet);
        round.start();
        round.hit(id, 0); // При seed 0 игрок перебирает.
        round.playDealer();
        round.settle();

        assertEquals(RoundPhase.FINISHED, round.getPhase());
        assertEquals(money("90"), player.getBalance());
    }

    @Test
    void blackjackPayoutTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(26))
        );
        game.placeBet(id, bet);
        game.start();
        game.settle();

        assertEquals(RoundPhase.FINISHED, game.getPhase());
        assertEquals(money("115"), player.getBalance());
    }
    @Test
    void surrenderPayoutTest() {
        round.placeBet(id, bet);
        round.start();

        GameAction action = new SurrenderAction(id, 0);
        action.execute(round);
        round.playDealer();
        round.settle();

        assertEquals(money("95"), player.getBalance());
        assertEquals(RoundPhase.FINISHED, round.getPhase());
        assertEquals(48, shoe.remainingCards());
    }
    @Test
    void doubleWinTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(6))
        );
        game.placeBet(id, bet);
        game.start();
        new DoubleAction(id, 0).execute(game);
        game.playDealer();
        game.settle();

        assertEquals(money("120"), player.getBalance());
        assertEquals(RoundPhase.FINISHED, game.getPhase());
    }

    @Test
    void doubleWithoutFundsTest() {
        round.placeBet(id, money("60"));
        round.start();

        assertThrows(InsufficientFundsException.class,
                () -> new DoubleAction(id, 0).execute(round));

        assertEquals(money("40"), player.getBalance());
        assertEquals(48, shoe.remainingCards());
        assertEquals(RoundPhase.PLAYER_TURNS, round.getPhase());
    }

    @Test
    void doubleWithEmptyShoeTest() {
        round.placeBet(id, bet);
        round.start();
        while (shoe.remainingCards() > 0) {
            shoe.draw();
        }

        assertThrows(ShoeEmptyException.class,
                () -> new DoubleAction(id, 0).execute(round));

        assertEquals(money("90"), player.getBalance());
        assertEquals(RoundPhase.PLAYER_TURNS, round.getPhase());
    }
    @Test
    void splitCreatesTwoTurnsTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(595))
        );
        game.placeBet(id, bet);
        game.start();
        new SplitAction(id, 0).execute(game);

        assertEquals(money("80"), player.getBalance());
        assertEquals(0, game.getCurrentHandIndex());

        game.stand(id, 0);
        assertEquals(1, game.getCurrentHandIndex());
    }

    @Test
    void splitAcesPayoutTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(2540))
        );
        game.placeBet(id, bet);
        game.start();
        new SplitAction(id, 0).execute(game);

        assertEquals(RoundPhase.DEALER_TURN, game.getPhase());
        game.playDealer();
        game.settle();

        assertEquals(money("100"), player.getBalance());
        assertEquals(RoundPhase.FINISHED, game.getPhase());
    }

    @Test
    void splitWithoutFundsTest() {
        Shoe splitShoe = new Shoe(1, new Random(595));
        BlackjackRound game = new BlackjackRound(List.of(player), splitShoe);
        game.placeBet(id, money("60"));
        game.start();

        assertThrows(InsufficientFundsException.class,
                () -> new SplitAction(id, 0).execute(game));

        assertEquals(money("40"), player.getBalance());
        assertEquals(48, splitShoe.remainingCards());
        assertEquals(0, game.getCurrentHandIndex());
    }
    @Test
    void insuranceWinsTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(55))
        );
        game.placeBet(id, bet);
        game.start();
        game.buyInsurance(id, money("5"));

        assertEquals(RoundPhase.SETTLEMENT, game.getPhase());
        assertEquals(money("100"), player.getBalance());

        game.settle();
        assertEquals(RoundPhase.FINISHED, game.getPhase());
        assertEquals(money("100"), player.getBalance());
    }

    @Test
    void insuranceWaitsForEveryoneTest() {
        Player second = new Player(new PlayerId(2), "Анна", money("100"));
        BlackjackRound game = new BlackjackRound(
                List.of(player, second), new Shoe(1, new Random(33))
        );
        game.placeBet(id, bet);
        game.placeBet(second.getId(), bet);
        game.start();
        game.buyInsurance(id, money("5"));

        assertEquals(RoundPhase.INSURANCE, game.getPhase());
        assertThrows(IllegalMoveException.class,
                () -> game.buyInsurance(id, money("1")));

        game.declineInsurance(second.getId());

        assertEquals(RoundPhase.PLAYER_TURNS, game.getPhase());
        assertEquals(id, game.getCurrentPlayerId());
        assertEquals(money("85"), player.getBalance());
    }

    @Test
    void playerWithBlackjackCanBuyInsuranceTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(327))
        );
        game.placeBet(id, bet);
        game.start();
        game.buyInsurance(id, money("5"));

        assertEquals(RoundPhase.SETTLEMENT, game.getPhase());
        game.settle();
        assertEquals(money("110"), player.getBalance());
    }
    @Test
    void dealerHiddenTest() {
        assertTrue(round.getVisibleDealerCards().isEmpty());

        round.placeBet(id, bet);
        round.start();

        assertFalse(round.isDealerRevealed());
        assertEquals(1, round.getVisibleDealerCards().size());
        assertThrows(IllegalMoveException.class,
                () -> round.getDealerTotal());
    }

    @Test
    void dealerRevealedOnTurnTest() {
        round.placeBet(id, bet);
        round.start();
        round.stand(id, 0);

        assertTrue(round.isDealerRevealed());
        assertEquals(2, round.getVisibleDealerCards().size());
        assertEquals(11, round.getDealerTotal());

        round.playDealer();
        assertEquals(22, round.getDealerTotal());
        assertEquals(4, round.getVisibleDealerCards().size());
    }

    @Test
    void dealerRevealedOnEarlySettlementTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(49))
        );
        game.placeBet(id, bet);
        game.start();

        assertTrue(game.isDealerRevealed());
        assertEquals(21, game.getDealerTotal());
        assertThrows(UnsupportedOperationException.class,
                () -> game.getVisibleDealerCards().clear());

        game.settle();
        assertTrue(game.isDealerRevealed());
    }
    @Test
    void betEventTest() {
        round.placeBet(id, bet);

        assertEquals(
                List.of(new BetPlacedEvent(id, bet)),
                round.getEvents()
        );
    }

    @Test
    void failedBetDoesNotCreateEventTest() {
        assertThrows(InsufficientFundsException.class,
                () -> round.placeBet(id, money("101")));
        assertTrue(round.getEvents().isEmpty());

        round.placeBet(id, bet);
        assertThrows(IllegalMoveException.class,
                () -> round.placeBet(id, bet));
        assertEquals(1, round.getEvents().size());
    }

    @Test
    void eventSnapshotTest() {
        List<GameEvent> before = round.getEvents();
        round.placeBet(id, bet);

        assertTrue(before.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> round.getEvents().clear());
    }
    @Test
    void phaseEventsTest() {
        round.placeBet(id, bet);
        round.start();

        assertTrue(round.getEvents().contains(new PhaseChangedEvent(
                RoundPhase.BETTING, RoundPhase.INITIAL_DEAL
        )));
        assertTrue(round.getEvents().contains(new PhaseChangedEvent(
                RoundPhase.INITIAL_DEAL, RoundPhase.PLAYER_TURNS
        )));
    }

    @Test
    void failedStartDoesNotChangeJournalTest() {
        List<GameEvent> before = round.getEvents();

        assertThrows(IllegalMoveException.class, () -> round.start());

        assertEquals(before, round.getEvents());
        assertEquals(RoundPhase.BETTING, round.getPhase());
    }

    @Test
    void hitCanKeepPlayerPhaseTest() {
        BlackjackRound game = new BlackjackRound(
                List.of(player), new Shoe(1, new Random(6))
        );
        game.placeBet(id, bet);
        game.start();
        game.hit(id, 0); // Было 16, после добора — 18.

        assertEquals(RoundPhase.PLAYER_TURNS, game.getPhase());
        assertTrue(game.getEvents().contains(
                new PlayerActionEvent(id, 0, ActionType.HIT)
        ));
    }
    @Test
    void actionPrecedesPhaseEventTest() {
        round.placeBet(id, bet);
        round.start();
        round.stand(id, 0);

        List<GameEvent> journal = round.getEvents();
        GameEvent action = new PlayerActionEvent(id, 0, ActionType.STAND);
        GameEvent transition = new PhaseChangedEvent(
                RoundPhase.PLAYER_TURNS, RoundPhase.DEALER_TURN
        );

        assertTrue(journal.contains(action));
        assertTrue(journal.indexOf(action) < journal.indexOf(transition));
    }

    @Test
    void failedActionDoesNotChangeJournalTest() {
        round.placeBet(id, bet);
        round.start();
        List<GameEvent> before = round.getEvents();

        assertThrows(IllegalMoveException.class,
                () -> round.hit(new PlayerId(2), 0));

        assertEquals(before, round.getEvents());
        assertEquals(48, shoe.remainingCards());
    }
}