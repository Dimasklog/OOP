package ru.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты прямой поездки.
 */
class DirectRouteTest {
    /**
     * Время в пути равно сумме участков между посадкой и высадкой.
     */
    @Test
    void journeyTimeBetweenStops(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        route.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        DirectRoute fullJourney = new DirectRoute(route, trip, park, station);
        DirectRoute partialJourney = new DirectRoute(route, trip, library, station);
        assertEquals(Duration.ofMinutes(15), fullJourney.allTimeInJourney());
        assertEquals(Duration.ofMinutes(5), partialJourney.allTimeInJourney());
    }

    /**
     * У прямой поездки ноль пересадок и она всегда осуществима.
     */
    @Test
    void zeroTransfersAndFeasible(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        DirectRoute journey = new DirectRoute(route, trip, park, library);
        assertEquals(0, journey.countTransfer());
        assertTrue(journey.feasibility());
    }

    /**
     * Любой из четырёх компонентов null бросает исключение аргумента.
     */
    @Test
    void nullComponentThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        assertThrows(IllegalArgumentException.class, () -> new DirectRoute(null, trip, park, library));
        assertThrows(IllegalArgumentException.class, () -> new DirectRoute(route, null, park, library));
        assertThrows(IllegalArgumentException.class, () -> new DirectRoute(route, trip, null, library));
        assertThrows(IllegalArgumentException.class, () -> new DirectRoute(route, trip, park, null));
    }

    /**
     * Рейс с чужим кодом маршрута бросает исключение чужого рейса.
     */
    @Test
    void foreignTripThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        Route route = new Route(1, "7", TransportType.BUS, park);
        Trip foreignTrip = new Trip(2, LocalTime.of(13, 0), DayType.WEEKDAY);
        assertThrows(ForeignTripException.class, () -> new DirectRoute(route, foreignTrip, park, park));
    }

    /**
     * Остановка вне маршрута бросает исключение остановки.
     */
    @Test
    void stopNotInRouteThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop outsideStop = new TransportStop(120, "Вокзал");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        assertThrows(TransportStopNotInRouteException.class, () -> new DirectRoute(route, trip, outsideStop, library));
        assertThrows(TransportStopNotInRouteException.class, () -> new DirectRoute(route, trip, park, outsideStop));
    }

    /**
     * Посадка не раньше высадки: равенство и обратный порядок бросают исключение индекса.
     */
    @Test
    void landingNotBeforeLeavingThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        assertThrows(RangeIndexException.class, () -> new DirectRoute(route, trip, park, park));
        assertThrows(RangeIndexException.class, () -> new DirectRoute(route, trip, library, park));
    }
}