package ru.example;

import java.time.Duration;

/**
 * Интерфейс поездки.
 */
public interface Journey {
    /**
     * Минимальное время на пересадку.
     */
    Duration timeTransfer = Duration.ofMinutes(1);

    /**
     * Возвращает время поездки.
     * @return сумма всех интервалов между остановкой посадки и остановкой выхода
     */
    Duration allTimeInJourney();

    /**
     * Количество пересадок.
     * @return количество пересадок
     */
    int countTransfer();

    /**
     * Осуществление поездки.
     * @return может ли быть осуществлена поездка
     */
    boolean feasibility();
}
