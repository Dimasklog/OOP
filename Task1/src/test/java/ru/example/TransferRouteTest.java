package ru.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты поездки с пересадкой.
 */
class TransferRouteTest {
    /**
     * Несовпадение остановки высадки первого плеча и посадки второго бросает исключение аргумента.
     */
    @Test
    void differentTransferStopsThrow(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.BUS, station);
        secondRoute.extensionTransportStop(library, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 20), DayType.WEEKDAY);
        DirectRoute firstLeg = new DirectRoute(firstRoute, firstTrip, park, library);
        DirectRoute secondLeg = new DirectRoute(secondRoute, secondTrip, station, library);
        assertThrows(IllegalArgumentException.class, () -> new TransferRoute(firstLeg, secondLeg));
    }

    /**
     * На кольцевом маршруте поездка моделируется до первого вхождения остановки высадки:
     * участок в пределах первого вхождения работает, полный круг и высадка на повторной
     * остановке отклоняются.
     */
    @Test
    void ringRouteLegRestriction(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        route.extensionTransportStop(park, Duration.ofMinutes(5));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        DirectRoute partialLeg = new DirectRoute(route, trip, park, library);
        assertEquals(Duration.ofMinutes(10), partialLeg.allTimeInJourney());
        assertThrows(RangeIndexException.class, () -> new DirectRoute(route, trip, park, park));
        assertThrows(RangeIndexException.class, () -> new DirectRoute(route, trip, library, park));
    }

    /**
     * Разрыв ровно в норматив осуществим: граница включается.
     */
    @Test
    void feasibleTransferAtExactlyNormative(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.BUS, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 11), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        DirectRoute firstLeg = new DirectRoute(firstRoute, firstTrip, park, library);
        DirectRoute secondLeg = new DirectRoute(secondRoute, secondTrip, library, station);
        TransferRoute journey = new TransferRoute(firstLeg, secondLeg);
        assertEquals(Duration.ofMinutes(1), journey.transferGap());
        assertTrue(journey.feasibility());
        assertEquals(Duration.ofMinutes(15), journey.allTimeInJourney());
        assertEquals(1, journey.countTransfer());
    }

    /**
     * Разрыв в пятьдесят девять секунд неосуществим: граница норматива строгая снизу.
     */
    @Test
    void shortTransferNotFeasible(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.BUS, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 10, 59), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        DirectRoute firstLeg = new DirectRoute(firstRoute, firstTrip, park, library);
        DirectRoute secondLeg = new DirectRoute(secondRoute, secondTrip, library, station);
        TransferRoute journey = new TransferRoute(firstLeg, secondLeg);
        assertEquals(Duration.ofSeconds(59), journey.transferGap());
        assertFalse(journey.feasibility());
    }

    /**
     * Отрицательный разрыв возвращается как есть и делает пересадку неосуществимой.
     */
    @Test
    void negativeTransferNotFeasible(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.BUS, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 9), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        DirectRoute firstLeg = new DirectRoute(firstRoute, firstTrip, park, library);
        DirectRoute secondLeg = new DirectRoute(secondRoute, secondTrip, library, station);
        TransferRoute journey = new TransferRoute(firstLeg, secondLeg);
        assertTrue(journey.transferGap().isNegative());
        assertFalse(journey.feasibility());
    }

    /**
     * Запрос общего времени у неосуществимой поездки бросает исключение.
     */
    @Test
    void timeRequestOnInfeasibleThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.BUS, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 9), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        DirectRoute firstLeg = new DirectRoute(firstRoute, firstTrip, park, library);
        DirectRoute secondLeg = new DirectRoute(secondRoute, secondTrip, library, station);
        TransferRoute journey = new TransferRoute(firstLeg, secondLeg);
        assertThrows(FeasibilityJourneyException.class, journey::allTimeInJourney);
    }

    /**
     * Любое плечо null бросает исключение аргумента.
     */
    @Test
    void nullLegThrows(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        DirectRoute leg = new DirectRoute(route, trip, park, library);
        assertThrows(IllegalArgumentException.class, () -> new TransferRoute(null, leg));
        assertThrows(IllegalArgumentException.class, () -> new TransferRoute(leg, null));
    }
}