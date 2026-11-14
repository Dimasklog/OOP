package org.example.blackjack.model.enums;

/**
 * Виды действий игрока с основной рукой.
 */
public enum ActionType {

    /** Добор карты. */
    HIT,

    /** Остановка руки. */
    STAND,

    /** Удвоение ставки с получением одной последней карты. */
    DOUBLE,

    /** Разделение пары. */
    SPLIT,

    /** Сдача руки. */
    SURRENDER
}