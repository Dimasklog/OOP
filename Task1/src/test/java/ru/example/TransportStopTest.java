package ru.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты остановки.
 */

class TransportStopTest {
    /**
     * Разные объекты, с одинаковыми значениями равны, с разными различны.
     */
    @Test
    void equalsToStops(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(101, "Парк");
        TransportStop thirdStop = new TransportStop(110, "Парк");
        assertEquals(firstStop, firstStop);
        assertEquals(firstStop, secondStop);
        assertEquals(secondStop, firstStop);
        assertEquals(firstStop.hashCode(), secondStop.hashCode());
        assertNotEquals(firstStop, thirdStop);
        assertNotEquals(new TransportStop(101, "Библиотека"), firstStop);
        assertNotEquals(null, firstStop);
    }

    /**
     * Код остановки положителен.
     */
    @Test
    void codeTest(){
        assertThrows(IllegalArgumentException.class, () -> new TransportStop(-1, "Парк"));
        assertThrows(IllegalArgumentException.class, () -> new TransportStop(0, "Парк"));
    }

    /**
     * Имя остановки не пустое.
     */
    @Test
    void nameTest(){
        assertThrows(IllegalArgumentException.class, () -> new TransportStop(101, null));
        assertThrows(IllegalArgumentException.class, () -> new TransportStop(101, ""));
        assertThrows(IllegalArgumentException.class, () -> new TransportStop(101, "      "));
    }

    /**
     * Поля объекта совпадают со значениями.
     */
    @Test
    void classTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        assertEquals(101, firstStop.code());
        assertEquals("Парк", firstStop.name());
    }
}