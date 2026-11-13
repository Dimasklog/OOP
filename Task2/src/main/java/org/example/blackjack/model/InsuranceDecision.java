package org.example.blackjack.model;

import org.example.blackjack.model.enums.InsuranceStatus;

import java.math.BigDecimal;

/**
 * Решение игрока о страховке и соответствующая сумма.
 *
 * @param status состояние решения
 * @param amount сумма страховки; для ожидания и отказа равна нулю
 */
public record InsuranceDecision(InsuranceStatus status, Money amount) {

    /**
     * Создает решение о страховке.
     *
     * @param status состояние решения
     * @param amount сумма страховки
     * @throws IllegalArgumentException если данные null или сумма не соответствует состоянию
     */
    public InsuranceDecision {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }

        if (status == InsuranceStatus.TAKEN && amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        } else if (status != InsuranceStatus.TAKEN && amount.amount().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("amount must be zero");
        }
    }
}