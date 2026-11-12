package org.example;

import org.example.blackjack.model.Money;
import org.example.blackjack.model.enums.HandOutcome;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HandOutcomeTests {
    private final Money bet = money("10");

    @Test
    void ordinaryPayoutsTest() {
        assertEquals(money("0"), HandOutcome.LOSS.payout(bet));
        assertEquals(money("10"), HandOutcome.PUSH.payout(bet));
        assertEquals(money("20"), HandOutcome.WIN.payout(bet));
    }

    @Test
    void fractionalPayoutsTest() {
        assertEquals(money("25"), HandOutcome.BLACKJACK.payout(bet));
        assertEquals(money("5"), HandOutcome.SURRENDER.payout(bet));
    }

    @Test
    void invalidBetTest() {
        assertThrows(IllegalArgumentException.class,
                () -> HandOutcome.WIN.payout(null));
        assertThrows(IllegalArgumentException.class,
                () -> HandOutcome.WIN.payout(money("0")));
    }

    private Money money(String value) {
        return new Money(new BigDecimal(value));
    }
}