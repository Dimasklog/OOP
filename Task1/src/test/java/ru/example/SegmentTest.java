package ru.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты сегментов маршрута.
 */
class SegmentTest {
    /**
     * Одинаковые сегменты равны и их хэш коды равны.
     */
    @Test
    void equalsSegment(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        TransportStop thirdStop = new TransportStop(111, "Дом культуры");
        Segment firstSeg = new Segment(firstStop, secondStop, Duration.ofMinutes(10));
        Segment secondSeg = new Segment(firstStop, secondStop, Duration.ofMinutes(10));
        Segment thirdSeg = new Segment(firstStop, secondStop, Duration.ofMinutes(20));
        Segment fourthSeg = new Segment(firstStop, thirdStop, Duration.ofMinutes(10));
        assertEquals(firstSeg, secondSeg);
        assertEquals(firstSeg.hashCode(), secondSeg.hashCode());
        assertNotEquals(firstSeg, thirdSeg);
        assertNotEquals(firstSeg, fourthSeg);
    }

    /**
     * Сегмент не null.
     */
    @Test
    void notNull(){
        TransportStop firstStop = null;
        TransportStop secondStop = null;
        TransportStop thirdStop = new TransportStop(101, "Парк");
        assertThrows(IllegalArgumentException.class, () -> new Segment(firstStop, thirdStop, Duration.ofMinutes(10)));
        assertThrows(IllegalArgumentException.class, () -> new Segment(thirdStop, firstStop, Duration.ofMinutes(10)));
        assertThrows(IllegalArgumentException.class, () -> new Segment(firstStop, secondStop, Duration.ofMinutes(10)));
    }

    /**
     * Сегмент с одинаковым началом и концом.
     */
    @Test
    void zeroSegment(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(101, "Парк");
        assertThrows(IllegalArgumentException.class, () -> new Segment(firstStop, secondStop, Duration.ofMinutes(10)));
    }

    /**
     * Сегмент с нулевым и отрицательным временем.
     */
    @Test
    void testWhenDurationZeroOrNegative(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        assertThrows(IllegalArgumentException.class, () -> new Segment(firstStop, secondStop, Duration.ofMinutes(0)));
        assertThrows(IllegalArgumentException.class, () -> new Segment(firstStop, secondStop, Duration.ofMinutes(-10)));
    }

    /**
     * Поля класса равны объектам.
     */
    @Test
    void testField(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Segment firstSeg = new Segment(firstStop, secondStop, Duration.ofMinutes(10));
        assertSame(firstStop, firstSeg.stopFrom());
        assertSame(secondStop, firstSeg.stopTo());
        assertEquals(Duration.ofMinutes(10), firstSeg.duration());
    }
}
