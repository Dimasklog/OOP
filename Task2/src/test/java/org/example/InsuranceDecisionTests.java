package org.example;

import org.example.blackjack.model.InsuranceDecision;
import org.example.blackjack.model.Money;
import org.example.blackjack.model.enums.InsuranceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceDecisionTests {
    private final Money zero = new Money(BigDecimal.ZERO);
    private final Money five = new Money(new BigDecimal("5"));

    @Test
    void validDecisionsTest() {
        assertFalse(new InsuranceDecision(
                InsuranceStatus.PENDING, zero).status().isDecided());
        assertTrue(new InsuranceDecision(
                InsuranceStatus.DECLINED, zero).status().isDecided());
        assertTrue(new InsuranceDecision(
                InsuranceStatus.TAKEN, five).status().isDecided());
    }

    @Test
    void invalidAmountsTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new InsuranceDecision(InsuranceStatus.TAKEN, zero));
        assertThrows(IllegalArgumentException.class,
                () -> new InsuranceDecision(InsuranceStatus.PENDING, five));
        assertThrows(IllegalArgumentException.class,
                () -> new InsuranceDecision(InsuranceStatus.DECLINED, five));
    }

    @Test
    void nullArgumentsTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new InsuranceDecision(null, zero));
        assertThrows(IllegalArgumentException.class,
                () -> new InsuranceDecision(InsuranceStatus.PENDING, null));
    }
}