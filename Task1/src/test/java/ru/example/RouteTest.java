package ru.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты маршрутов.
 */
class RouteTest {
    /**
     * Проверка списка остановок и сегмента маршрута.
     */
    @Test
    void testStopAndSegment(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        assertEquals(route.getTransportStopList(), new ArrayList<>(List.of(new TransportStop[]{firstStop, secondStop})));
        assertEquals(route.getSegmentList().getFirst(), new Segment(firstStop, secondStop, Duration.ofMinutes(10)));
    }

    /**
     * При добавлении дубликата бросает исключение.
     */
    @Test
    void duplicateTripTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        route.addTrip(new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF));
        assertThrows(DuplicateTripException.class, () -> route.addTrip(new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF)));
    }

    /**
     * При добавлении чужого рейса бросает исключение.
     */
    @Test
    void foreignTripTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        assertThrows(ForeignTripException.class, () -> route.addTrip(new Trip(100, LocalTime.of(13, 0, 0), DayType.DAYOFF)));
    }

    /**
     * При попытке доехать по чужому рейсу бросает исключение.
     */
    @Test
    void foreignTripWhenArrival(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        assertThrows(ForeignTripException.class, () -> route.arrivalKStop(new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF), 1));
    }

    /**
     * Проверка поиска первой остановки.
     */
    @Test
    void firstTransportStopTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        LocalTime lt = LocalTime.of(13, 0, 0);
        Trip trip = new Trip(121, lt, DayType.DAYOFF);
        route.addTrip(trip);
        assertEquals(route.arrivalKStop(trip, 0), lt);
    }

    /**
     * Проверка поиска последней остановки.
     */
    @Test
    void lastTransportStopTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        Duration dur = Duration.ofMinutes(10);
        route.extensionTransportStop(secondStop, dur);
        LocalTime lt = LocalTime.of(13, 0, 0);
        Trip trip = new Trip(121, lt, DayType.DAYOFF);
        route.addTrip(trip);
        assertEquals(route.arrivalKStop(trip, 1), lt.plus(dur));
        assertEquals(route.timeAllRoute(), dur);
    }

    /**
     * При попытке поиска времени прибытия вне интервала бросает исключение.
     */
    @Test
    void indexRangeTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        Trip trip = new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF);
        route.addTrip(trip);
        assertThrows(RangeIndexException.class, () -> route.arrivalKStop(trip, 5));
        assertThrows(RangeIndexException.class, () -> route.arrivalKStop(trip, 2));
        assertThrows(RangeIndexException.class, () -> route.arrivalKStop(trip, -5));
    }

    /**
     * Проверка интервалов.
     */
    @Test
    void intervalsTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        Trip firstTrip = new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF);
        Trip secondTrip = new Trip(121, LocalTime.of(13, 10, 0), DayType.DAYOFF);
        Trip thirdTrip = new Trip(121, LocalTime.of(13, 20, 0), DayType.DAYOFF);
        route.addTrip(thirdTrip);
        route.addTrip(firstTrip);
        route.addTrip(secondTrip);
        assertEquals(route.intervals(DayType.DAYOFF), new ArrayList<>(List.of(new Duration[] {Duration.ofMinutes(10), Duration.ofMinutes(10)})));
    }

    /**
     * При попытке вызова поиска интервалов, если рейсов нет или он один, бросает исключение
     */
    @Test
    void intervalCountTest(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        Trip firstTrip = new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF);
        assertThrows(LessTripException.class, () -> route.intervals(DayType.DAYOFF));
        route.addTrip(firstTrip);
        assertThrows(LessTripException.class, () -> route.intervals(DayType.DAYOFF));
    }

    /**
     * Возвращаемые списки нельзя редактировать.
     */
    @Test
    void returnedListsAreImmutable(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(121, "37", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        assertThrows(UnsupportedOperationException.class, () -> route.getSegmentList().addLast(new Segment(firstStop, secondStop, Duration.ofMinutes(10))));
        assertThrows(UnsupportedOperationException.class, () -> route.getTripList().addLast(new Trip(121, LocalTime.of(13, 0, 0), DayType.DAYOFF)));
        assertThrows(UnsupportedOperationException.class, () -> route.getTransportStopList().addLast(new TransportStop(141, "Театр")));
    }

    /**
     * Совпадение расчета времени по индексу и объекту остановки
     */
    @Test
    void arrivalByStopEqualsByIndex(){
        TransportStop firstStop = new TransportStop(101, "Парк");
        TransportStop secondStop = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, firstStop);
        route.extensionTransportStop(secondStop, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(8, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        assertEquals(route.arrivalKStop(trip, 0), route.arrivalNameStop(trip, firstStop));
        assertEquals(route.arrivalKStop(trip, 1), route.arrivalNameStop(trip, secondStop));
        assertThrows(TransportStopNotInRouteException.class, () -> route.arrivalNameStop(trip, new TransportStop(120, "Вокзал")));
    }

    /**
     * Кольцевой маршрут: остановка повторяется, объектная форма считает до первого вхождения.
     */
    @Test
    void ringRouteUsesFirstOccurrence(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        route.extensionTransportStop(park, Duration.ofMinutes(5));
        Trip trip = new Trip(1, LocalTime.of(8, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        assertEquals(LocalTime.of(8, 0), route.arrivalNameStop(trip, park));
        assertEquals(LocalTime.of(8, 15), route.arrivalKStop(trip, 2));
    }
}
