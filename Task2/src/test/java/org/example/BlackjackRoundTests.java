package org.example;

import org.example.blackjack.model.*;
import org.example.blackjack.model.actions.*;
import org.example.blackjack.model.enums.RoundPhase;
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
}