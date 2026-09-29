package ru.example;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты записи расписания.
 */
class ScheduleEntryTest {
    /**
     * Любой из трёх компонентов null бросает исключение аргумента.
     */
    @Test
    void nullComponentThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        Route route = new Route(1, "7", TransportType.BUS, park);
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        LocalTime arrivalTime = LocalTime.of(13, 10);
        assertThrows(IllegalArgumentException.class, () -> new ScheduleEntry(null, trip, arrivalTime));
        assertThrows(IllegalArgumentException.class, () -> new ScheduleEntry(route, null, arrivalTime));
        assertThrows(IllegalArgumentException.class, () -> new ScheduleEntry(route, trip, null));
    }

    /**
     * Валидная запись возвращает переданные компоненты.
     */
    @Test
    void entryReturnsComponents(){
        TransportStop park = new TransportStop(101, "Парк");
        Route route = new Route(1, "7", TransportType.BUS, park);
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        LocalTime arrivalTime = LocalTime.of(13, 10);
        ScheduleEntry entry = new ScheduleEntry(route, trip, arrivalTime);
        assertEquals(route, entry.route());
        assertEquals(trip, entry.trip());
        assertEquals(arrivalTime, entry.arrivalTime());
    }
}