package ru.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты сети транспорта.
 */
class TransportNetworkTest {
    /**
     * Добавленные маршруты видны в просмотре.
     */
    @Test
    void addRouteAddsToGetRouteList(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        Route secondRoute = new Route(2, "37", TransportType.TRAM, library);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        network.addRoute(secondRoute);
        assertEquals(List.of(firstRoute, secondRoute), network.getRouteList());
    }

    /**
     * Null-маршрут бросает исключение аргумента.
     */
    @Test
    void addRouteRejectsNull(){
        TransportNetwork network = new TransportNetwork();
        assertThrows(IllegalArgumentException.class, () -> network.addRoute(null));
    }

    /**
     * Маршрут с занятым кодом бросает исключение аргумента.
     */
    @Test
    void addRouteRejectsSameCode(){
        TransportStop park = new TransportStop(101, "Парк");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        Route secondRoute = new Route(1, "8", TransportType.TRAM, park);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        assertThrows(IllegalArgumentException.class, () -> network.addRoute(secondRoute));
    }

    /**
     * Список просмотра защищён от правки.
     */
    @Test
    void getRouteListIsImmutable(){
        TransportStop park = new TransportStop(101, "Парк");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        List<Route> routeList = network.getRouteList();
        assertThrows(UnsupportedOperationException.class, () -> routeList.addLast(firstRoute));
    }

    /**
     * Расписание сортируется по времени прибытия через маршруты разной длины.
     */
    @Test
    void timetableSortedByArrivalTime(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route longRoute = new Route(1, "7", TransportType.BUS, park);
        longRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        longRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Route shortRoute = new Route(2, "37", TransportType.TRAM, park);
        shortRoute.extensionTransportStop(station, Duration.ofMinutes(8));
        Trip longTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        longRoute.addTrip(longTrip);
        Trip shortTrip = new Trip(2, LocalTime.of(13, 5), DayType.WEEKDAY);
        shortRoute.addTrip(shortTrip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(longRoute);
        network.addRoute(shortRoute);
        List<ScheduleEntry> timetable = network.timetable(station, DayType.WEEKDAY);
        assertEquals(2, timetable.size());
        assertEquals(LocalTime.of(13, 13), timetable.getFirst().arrivalTime());
        assertEquals(shortRoute, timetable.getFirst().route());
        assertEquals(LocalTime.of(13, 15), timetable.get(1).arrivalTime());
        assertEquals(longRoute, timetable.get(1).route());
    }

    /**
     * Расписание возвращает рейсы только запрошенного признака дня.
     */
    @Test
    void timetableFiltersByDayType(){
        TransportStop park = new TransportStop(101, "Парк");
        Route weekdayRoute = new Route(1, "7", TransportType.BUS, park);
        Route dayoffRoute = new Route(2, "37", TransportType.TRAM, park);
        Trip weekdayTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        Trip dayoffTrip = new Trip(2, LocalTime.of(10, 0), DayType.DAYOFF);
        weekdayRoute.addTrip(weekdayTrip);
        dayoffRoute.addTrip(dayoffTrip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(weekdayRoute);
        network.addRoute(dayoffRoute);
        List<ScheduleEntry> weekdayTimetable = network.timetable(park, DayType.WEEKDAY);
        List<ScheduleEntry> dayoffTimetable = network.timetable(park, DayType.DAYOFF);
        assertEquals(1, weekdayTimetable.size());
        assertEquals(weekdayTrip, weekdayTimetable.getFirst().trip());
        assertEquals(1, dayoffTimetable.size());
        assertEquals(dayoffTrip, dayoffTimetable.getFirst().trip());
    }

    /**
     * Остановка вне сети даёт пустое расписание, а не исключение.
     */
    @Test
    void timetableEmptyForUnknownStop(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop market = new TransportStop(130, "Рынок");
        Route route = new Route(1, "7", TransportType.BUS, park);
        Trip trip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        route.addTrip(trip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(route);
        assertTrue(network.timetable(market, DayType.WEEKDAY).isEmpty());
    }

    /**
     * Null-остановка и null-признак дня бросают исключение аргумента.
     */
    @Test
    void timetableRejectsNull(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportNetwork network = new TransportNetwork();
        assertThrows(IllegalArgumentException.class, () -> network.timetable(null, DayType.WEEKDAY));
        assertThrows(IllegalArgumentException.class, () -> network.timetable(park, null));
    }

    /**
     * Прямая связь находится во всех маршрутах с нужным порядком остановок.
     */
    @Test
    void directRoutesBetweenFound(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        firstRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Route secondRoute = new Route(2, "37", TransportType.TRAM, park);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(8));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        network.addRoute(secondRoute);
        List<Route> found = network.directRoutesBetween(park, station);
        assertEquals(2, found.size());
        assertTrue(found.contains(firstRoute));
        assertTrue(found.contains(secondRoute));
    }

    /**
     * Порядок остановок важен: обратное направление связи не даёт.
     */
    @Test
    void directRoutesBetweenReverseOrderEmpty(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route route = new Route(1, "7", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(10));
        route.extensionTransportStop(station, Duration.ofMinutes(5));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(route);
        assertTrue(network.directRoutesBetween(station, park).isEmpty());
        assertTrue(network.directRoutesBetween(station, library).isEmpty());
    }

    /**
     * Null-аргументы бросают исключение аргумента.
     */
    @Test
    void directRoutesBetweenRejectsNull(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportNetwork network = new TransportNetwork();
        assertThrows(IllegalArgumentException.class, () -> network.directRoutesBetween(null, park));
        assertThrows(IllegalArgumentException.class, () -> network.directRoutesBetween(park, null));
    }

    /**
     * Кольцевой маршрут находится парой через повторное вхождение остановки.
     */
    @Test
    void directRoutesBetweenRingFound(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route ringRoute = new Route(3, "5", TransportType.BUS, park);
        ringRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        ringRoute.extensionTransportStop(park, Duration.ofMinutes(5));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(ringRoute);
        List<Route> found = network.directRoutesBetween(library, park);
        assertEquals(1, found.size());
        assertEquals(ringRoute, found.getFirst());
    }

    /**
     * Маршрут с несколькими подходящими парами добавляется один раз.
     */
    @Test
    void directRoutesBetweenNoDuplicates(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        Route route = new Route(4, "9", TransportType.BUS, park);
        route.extensionTransportStop(library, Duration.ofMinutes(5));
        route.extensionTransportStop(park, Duration.ofMinutes(5));
        route.extensionTransportStop(library, Duration.ofMinutes(5));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(route);
        List<Route> found = network.directRoutesBetween(park, library);
        assertEquals(1, found.size());
    }

    /**
     * Осуществимая поездка с пересадкой находится.
     */
    @Test
    void transferRouteListFindsJourney(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.TRAM, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 11), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        network.addRoute(secondRoute);
        List<TransferRoute> journeys = network.transferRouteList(park, station, DayType.WEEKDAY);
        assertEquals(1, journeys.size());
        TransferRoute journey = journeys.getFirst();
        assertEquals(park, journey.firstRoute().landingTransportStop());
        assertEquals(station, journey.secondRoute().leavingTransportStop());
        assertEquals(library, journey.firstRoute().leavingTransportStop());
        assertEquals(Duration.ofMinutes(15), journey.allTimeInJourney());
    }

    /**
     * Разрыв меньше норматива пересадки отсеивает вариант.
     */
    @Test
    void transferRouteListRejectsShortGap(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route secondRoute = new Route(2, "37", TransportType.TRAM, library);
        secondRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip secondTrip = new Trip(2, LocalTime.of(13, 10), DayType.WEEKDAY);
        secondRoute.addTrip(secondTrip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        network.addRoute(secondRoute);
        assertTrue(network.transferRouteList(park, station, DayType.WEEKDAY).isEmpty());
    }

    /**
     * Отсутствие связи через пересадку даёт пустой список.
     */
    @Test
    void transferRouteListEmptyWithoutConnection(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        assertTrue(network.transferRouteList(park, station, DayType.WEEKDAY).isEmpty());
    }

    /**
     * Null-аргументы бросают исключение аргумента.
     */
    @Test
    void transferRouteListRejectsNull(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportNetwork network = new TransportNetwork();
        assertThrows(IllegalArgumentException.class, () -> network.transferRouteList(null, park, DayType.WEEKDAY));
        assertThrows(IllegalArgumentException.class, () -> network.transferRouteList(park, null, DayType.WEEKDAY));
        assertThrows(IllegalArgumentException.class, () -> network.transferRouteList(park, park, null));
    }

    /**
     * Из двух вариантов выбирается поездка с меньшим общим временем.
     */
    @Test
    void transferRouteWithMinTimeSelectsFaster(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        Route fastRoute = new Route(2, "37", TransportType.TRAM, library);
        fastRoute.extensionTransportStop(station, Duration.ofMinutes(5));
        Route slowRoute = new Route(3, "48", TransportType.TRAM, library);
        slowRoute.extensionTransportStop(station, Duration.ofMinutes(12));
        Trip firstTrip = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        firstRoute.addTrip(firstTrip);
        Trip fastTrip = new Trip(2, LocalTime.of(13, 11), DayType.WEEKDAY);
        fastRoute.addTrip(fastTrip);
        Trip slowTrip = new Trip(3, LocalTime.of(13, 11), DayType.WEEKDAY);
        slowRoute.addTrip(slowTrip);
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        network.addRoute(fastRoute);
        network.addRoute(slowRoute);
        Optional<TransferRoute> best = network.transferRouteWithMinTime(park, station, DayType.WEEKDAY);
        assertTrue(best.isPresent());
        assertEquals(fastRoute, best.get().secondRoute().route());
        assertEquals(Duration.ofMinutes(15), best.get().allTimeInJourney());
    }

    /**
     * Отсутствие вариантов даёт пустой опционал.
     */
    @Test
    void transferRouteWithMinTimeEmptyWithoutJourney(){
        TransportStop park = new TransportStop(101, "Парк");
        TransportStop library = new TransportStop(110, "Библиотека");
        TransportStop station = new TransportStop(120, "Вокзал");
        Route firstRoute = new Route(1, "7", TransportType.BUS, park);
        firstRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        TransportNetwork network = new TransportNetwork();
        network.addRoute(firstRoute);
        assertTrue(network.transferRouteWithMinTime(park, station, DayType.WEEKDAY).isEmpty());
    }
}