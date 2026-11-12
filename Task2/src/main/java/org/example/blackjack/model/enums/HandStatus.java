package org.example.blackjack.model.enums;

/**
 * Состояние руки.
 */
public enum HandStatus {
    /**
     * Можно принимать решения.
     */
    ACTIVE,
    /**
     * Игрок остановился.
     */
    STOOD,
    /**
     * Перебор.
     */
    BUST,
    /**
     * Игрок сдался.
     */
    SURRENDERED;

    /**
     * Проверяет, закончены ли действия игрока этой рукой.
     *
     * @return true, если состояние отличается от ACTIVE
     */
    public boolean isFinished(){
        return this != ACTIVE;
    }
}
