package ru.example;

import java.time.Duration;

/**
 * Участок маршрута, две остановки и время хода между ними.
 * @param stopFrom начальная остановка
 * @param stopTo конечная остановка
 * @param duration время хода между остановками
 * @throws IllegalArgumentException если stopFrom, stopTo или duration null, если stopFrom совпадает с stopTo, если duration не положительное
 */
public record Segment(TransportStop stopFrom, TransportStop stopTo, Duration duration) {
    public Segment{
        if (stopFrom == null || stopTo == null){
            throw new IllegalArgumentException("Transport stops must be not null");
        }
        if (stopFrom.equals(stopTo)){
            throw new IllegalArgumentException("Transport stops must be different");
        }
        if (duration == null){
            throw new IllegalArgumentException("Duration must not be null");
        }
        if (duration.isNegative() || duration.isZero()){
            throw new IllegalArgumentException("Duration must be positive");
        }
    }
}
