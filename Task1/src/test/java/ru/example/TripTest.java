package ru.example;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты рейса.
 */
class TripTest {
    /**
     * Разные объекты с одинаковыми значениями равны, с разными различны.
     */
    @Test
    void equalsToTrips(){
        Trip firstTrip = new Trip(1, LocalTime.of(8, 0), DayType.WEEKDAY);
        Trip secondTrip = new Trip(1, LocalTime.of(8, 0), DayType.WEEKDAY);
        Trip thirdTrip = new Trip(2, LocalTime.of(8, 0), DayType.WEEKDAY);
        assertEquals(firstTrip, firstTrip);
        assertEquals(firstTrip, secondTrip);
        assertEquals(secondTrip, firstTrip);
        assertEquals(firstTrip.hashCode(), secondTrip.hashCode());
        assertNotEquals(firstTrip, thirdTrip);
        assertNotEquals(new Trip(1, LocalTime.of(8, 30), DayType.WEEKDAY), firstTrip);
        assertNotEquals(new Trip(1, LocalTime.of(8, 0), DayType.DAYOFF), firstTrip);
        assertNotEquals(null, firstTrip);
    }

    /**
     * Код маршрута положителен.
     */
    @Test
    void codeTest(){
        assertThrows(IllegalArgumentException.class, () -> new Trip(-1, LocalTime.of(8, 0), DayType.WEEKDAY));
        assertThrows(IllegalArgumentException.class, () -> new Trip(0, LocalTime.of(8, 0), DayType.WEEKDAY));
    }

    /**
     * Время отправления и тип дня не null.
     */
    @Test
    void notNull(){
        assertThrows(IllegalArgumentException.class, () -> new Trip(1, null, DayType.WEEKDAY));
        assertThrows(IllegalArgumentException.class, () -> new Trip(1, LocalTime.of(8, 0), null));
    }

    /**
     * Поля объекта совпадают со значениями.
     */
    @Test
    void fieldTest(){
        Trip firstTrip = new Trip(1, LocalTime.of(8, 0), DayType.WEEKDAY);
        assertEquals(1, firstTrip.codeOfRoute());
        assertEquals(LocalTime.of(8, 0), firstTrip.departureTime());
        assertSame(DayType.WEEKDAY, firstTrip.dayType());
    }
}