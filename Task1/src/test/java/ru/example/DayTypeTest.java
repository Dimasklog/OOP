package ru.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты перечисления {@link DayType}.
 */
class DayTypeTest {

    /**
     * Ровно две константы, и они различны между собой.
     */
    @Test
    void hasExactlyTwoDistinctConstants() {
        DayType[] values = DayType.values();
        assertEquals(2, values.length);
        assertNotEquals(values[0], values[1]);
    }

    /**
     * valueOf по несуществующему имени бросает IllegalArgumentException.
     */
    @Test
    void valueOfUnknownNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DayType.valueOf("HOLIDAY"));
    }
}