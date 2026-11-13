package org.example.blackjack.model.enums;

/**
 * Состояние решения игрока о страховке.
 */
public enum InsuranceStatus {

    /** Игрок еще не принял решение. */
    PENDING,

    /** Игрок отказался от страховки. */
    DECLINED,

    /** Игрок купил страховку. */
    TAKEN;

    /**
     * Проверяет, принято ли решение о страховке.
     *
     * @return true, если игрок купил страховку или отказался
     */
    public boolean isDecided() {
        return this != PENDING;
    }
}