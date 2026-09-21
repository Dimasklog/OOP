package ru.example;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты перечисления.
 */
class TransportTypeTest {

    /**
     * У каждой константы скорость и вместимость строго больше нуля.
     */
    @Test
    void allConstantsHavePositiveSpeedAndCapacity() {
        for (TransportType type : TransportType.values()) {
            assertTrue(type.getSpeed() > 0,
                    "speed must be positive for " + type);
            assertTrue(type.getCapacity() > 0,
                    "capacity must be positive for " + type);
        }
    }

    /**
     * В перечислении ровно четыре константы, защита от случайно добавленной.
     */
    @Test
    void hasExactlyFourConstants() {
        assertEquals(4, TransportType.values().length);
    }

    /**
     * valueOf по известному имени возвращает ту же самую константу.
     */
    @Test
    void valueOfKnownNameReturnsSameConstant() {
        assertSame(TransportType.BUS, TransportType.valueOf("BUS"));
    }

    /**
     * valueOf по несуществующему имени бросает IllegalArgumentException.
     */
    @Test
    void valueOfUnknownNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> TransportType.valueOf("PLANE"));
    }
}