package ru.example;

import java.time.Duration;
import java.time.LocalTime;

public class Main {

    /**
     * Форматирует длительность в читаемый вид (например, "15 мин" вместо "PT15M").
     */
    private static String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        return hours > 0 ? hours + " ч " + minutes + " мин" : minutes + " мин";
    }

    public static void main(String[] args) {
        var park = new TransportStop(101, "Парк");
        var library = new TransportStop(110, "Библиотека");
        var station = new TransportStop(120, "Вокзал");
        var market = new TransportStop(130, "Рынок");
        var university = new TransportStop(140, "Университет");
        var stadium = new TransportStop(150, "Стадион");
        var square = new TransportStop(160, "Площадь");
        var museum = new TransportStop(170, "Музей");
        var culturePark = new TransportStop(180, "Парк Культуры");
        var center = new TransportStop(190, "Центр");

        var busRoute = new Route(1, "7", TransportType.BUS, park);
        busRoute.extensionTransportStop(library, Duration.ofMinutes(10));
        busRoute.extensionTransportStop(station, Duration.ofMinutes(5));

        var tramRoute = new Route(2, "37", TransportType.TRAM, park);
        tramRoute.extensionTransportStop(market, Duration.ofMinutes(8));
        tramRoute.extensionTransportStop(station, Duration.ofMinutes(7));

        var tramRoute2 = new Route(3, "48", TransportType.TRAM, library);
        tramRoute2.extensionTransportStop(university, Duration.ofMinutes(12));
        tramRoute2.extensionTransportStop(station, Duration.ofMinutes(10));

        var metroRoute = new Route(4, "1", TransportType.METRO, center);
        metroRoute.extensionTransportStop(museum, Duration.ofMinutes(3));
        metroRoute.extensionTransportStop(square, Duration.ofMinutes(4));


        var busTrip1 = new Trip(1, LocalTime.of(13, 0), DayType.WEEKDAY);
        var busTrip2 = new Trip(1, LocalTime.of(13, 20), DayType.WEEKDAY);
        busRoute.addTrip(busTrip1);
        busRoute.addTrip(busTrip2);

        var tramTrip1 = new Trip(2, LocalTime.of(13, 5), DayType.WEEKDAY);
        var tramTrip2 = new Trip(2, LocalTime.of(10, 0), DayType.DAYOFF);
        tramRoute.addTrip(tramTrip1);
        tramRoute.addTrip(tramTrip2);

        var tram2Trip1 = new Trip(3, LocalTime.of(13, 11), DayType.WEEKDAY);
        tramRoute2.addTrip(tram2Trip1);

        var metroTrip1 = new Trip(4, LocalTime.of(14, 0), DayType.WEEKDAY);
        metroRoute.addTrip(metroTrip1);




        var network = new TransportNetwork();
        network.addRoute(busRoute);
        network.addRoute(tramRoute);
        network.addRoute(tramRoute2);
        network.addRoute(metroRoute);



        System.out.println("=== 1. Маршруты сети ===");
        for (var route : network.getRouteList()) {
            System.out.println("- Маршрут " + route.getRouteName() + " (" + route.getTransportType() + ")");
        }

        System.out.println("\n=== 2. Расписание по остановке и дню ===");
        System.out.println("-- Будний день на остановке Вокзал --");
        for (var entry : network.timetable(station, DayType.WEEKDAY)) {
            System.out.println("- Маршрут " + entry.route().getRouteName() + ", прибытие: " + entry.arrivalTime());
        }
        System.out.println("-- Выходной день на остановке Вокзал --");
        for (var entry : network.timetable(station, DayType.DAYOFF)) {
            System.out.println("- Маршрут " + entry.route().getRouteName() + ", прибытие: " + entry.arrivalTime());
        }

        System.out.println("\n=== 3. Расчётное время прибытия на k-ю остановку ===");
        var arrivalTime = busRoute.arrivalKStop(busTrip1, 2);
        System.out.println("- Рейс " + busTrip1.departureTime() + " по маршруту " + busRoute.getRouteName()
                + " прибудет на 3-ю остановку (индекс 2) в " + arrivalTime);

        System.out.println("\n=== 4. Интервал движения на маршруте ===");
        try {
            var intervals = busRoute.intervals(DayType.WEEKDAY);
            System.out.println("- Интервалы для маршрута " + busRoute.getRouteName() + " (будни):");
            for (var interval : intervals) {
                System.out.println("  " + formatDuration(interval));
            }
        } catch (LessTripException e) {
            System.out.println("- Недостаточно рейсов для расчёта интервала: " + e.getMessage());
        }

        System.out.println("\n=== 5. Маршруты без пересадки (Парк -> Вокзал) ===");
        for (var route : network.directRoutesBetween(park, station)) {
            System.out.println("- " + route.getRouteName());
        }

        System.out.println("\n=== 6. Общее время в пути (прямая поездка) ===");
        var directJourney = new DirectRoute(busRoute, busTrip1, park, station);
        System.out.println("- Время в пути: " + formatDuration(directJourney.allTimeInJourney()));



        System.out.println("\n=== 7. Поездка с пересадкой (Парк -> Вокзал) ===");
        var bestTransfer = network.transferRouteWithMinTime(park, station, DayType.WEEKDAY);


        if (bestTransfer.isPresent()) {
            Journey journey = bestTransfer.get();
            String journeyInfo = switch (journey) {
                case DirectRoute direct -> "Прямая поездка. Время: " + formatDuration(direct.allTimeInJourney());
                case TransferRoute transfer -> "Поездка с пересадкой. Время: " + formatDuration(transfer.allTimeInJourney())
                        + ", пересадка на: " + transfer.firstRoute().leavingTransportStop().name()
                        + ", время на пересадку: " + formatDuration(transfer.transferGap());
            };
            System.out.println("- " + journeyInfo);
        } else {
            System.out.println("- Осуществимых вариантов с пересадкой нет.");
        }


        System.out.println("\n=== 8. Обработка исключения неосуществимой поездки ===");
        try {
            var tripA = new Trip(1, LocalTime.of(15, 0), DayType.WEEKDAY);
            var tripB = new Trip(3, LocalTime.of(15, 10), DayType.WEEKDAY);
            busRoute.addTrip(tripA);
            tramRoute2.addTrip(tripB);

            var leg1 = new DirectRoute(busRoute, tripA, park, library);
            var leg2 = new DirectRoute(tramRoute2, tripB, library, station);
            var infeasibleJourney = new TransferRoute(leg1, leg2);
            
            System.out.println(infeasibleJourney.allTimeInJourney());
        } catch (FeasibilityJourneyException e) {
            System.out.println("Поездка не может быть осуществлена: " + e.getMessage());
        }
    }
}