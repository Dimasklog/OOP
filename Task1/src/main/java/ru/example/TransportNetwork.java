package ru.example;

import java.util.*;

/**
 * Транспортная сеть.
 */
public class TransportNetwork {
    private List<Route> routeList = new ArrayList<>();

    /**
     * Добавляет маршрут в сеть.
     * @param route добавляемый маршрут
     * @throws IllegalArgumentException если route null или если маршрут уже был добавлен
     */
    public void addRoute(Route route){
        if (route == null){
            throw new IllegalArgumentException("route must not be null");
        }
        for (Route r: routeList){
            if (r.getRouteCode() == route.getRouteCode()){
                throw new IllegalArgumentException("route must not repeat");
            }
        }
        routeList.addLast(route);
    }

    /**
     * Расписание по остановке и дню.
     * @param stop остановка, на которой необходимо узнать расписание
     * @param dayType день, в который необходимо узнать расписание
     * @return список расписаний по остановке
     * @throws IllegalArgumentException если stop или dayType null
     */
    public List<ScheduleEntry> timetable(TransportStop stop, DayType dayType){
        if (stop == null){
            throw new IllegalArgumentException("stop must not be null");
        }
        if (dayType == null){
            throw new IllegalArgumentException("type of day must not be null");
        }
        List<ScheduleEntry> scheduleEntryList = new ArrayList<>();
        for (Route r: routeList){
            if (r.getTransportStopList().contains(stop)){
                List<Trip> tripList = r.getTripList();
                for (Trip tr: tripList){
                    if (tr.dayType().equals(dayType)){
                        ScheduleEntry scheduleEntry = new ScheduleEntry(r, tr, r.arrivalNameStop(tr, stop));
                        scheduleEntryList.addLast(scheduleEntry);
                    }
                }
            }
        }
        scheduleEntryList.sort(new Comparator<ScheduleEntry>() {
            @Override
            public int compare(ScheduleEntry o1, ScheduleEntry o2) {
                return o1.arrivalTime().compareTo(o2.arrivalTime());
            }
        });
        return List.copyOf(scheduleEntryList);
    }

    /**
     * Находит прямые маршруты между двумя остановками.
     * @param firstStop остановка от которой необходимо искать
     * @param secondStop остановка до которой необходимо искать
     * @return возвращает список маршрутов, по которым можно добраться. Пустой список означает отсутствие связывающего маршрута
     * @throws IllegalArgumentException если firstStop или secondStop null
     */
    public List<Route> directRoutesBetween(TransportStop firstStop, TransportStop secondStop){
        if (firstStop == null || secondStop == null){
            throw new IllegalArgumentException("first and second stops must not be null");
        }
        List<Route> routeBetween = new ArrayList<>();
        out:
        for (Route rt: routeList){
            List<TransportStop> transportStopList = rt.getTransportStopList();
            for (int i = 0; i < transportStopList.size() - 1; i++){
                for (int j = i + 1; j < transportStopList.size(); j++){
                    if (firstStop.equals(transportStopList.get(i)) && secondStop.equals(transportStopList.get(j))){
                        routeBetween.addLast(rt);
                        continue out;
                    }
                }
            }
        }
        return List.copyOf(routeBetween);
    }
    private List<DirectRoute> legsBetween(TransportStop fromStop, TransportStop toStop, DayType dayType){
        if (fromStop.equals(toStop)){
            return List.of();
        }
        List<DirectRoute> legs = new ArrayList<>();
        for (Route rt: directRoutesBetween(fromStop, toStop)){
            for (Trip trip: rt.getTripList()){
                if (trip.dayType().equals(dayType)){
                    legs.addLast(new DirectRoute(rt, trip, fromStop, toStop));
                }
            }
        }
        return legs;
    }

    /**
     * Ищет список поездок с пересадками.
     * @param stopFrom остановка от которой ведется поиск
     * @param stopTo остановка до которой ведется поиск
     * @param dayType тип дня
     * @return список поездок с пересадкой. Список содержит только осуществимые поездки и может быть пуст
     * @throws IllegalArgumentException если stopFrom, stopTo или dayType null
     */
    public List<TransferRoute> transferRouteList(TransportStop stopFrom, TransportStop stopTo, DayType dayType){
        if (stopFrom == null || stopTo == null){
            throw new IllegalArgumentException("stopFrom or stopTo must not be null");
        }
        if (dayType == null) {
            throw new IllegalArgumentException("type of day must not be null");
        }

        Set<TransportStop> stopSet = new LinkedHashSet<>();
        for (Route rt: routeList){
            for (TransportStop ts: rt.getTransportStopList()){
                stopSet.add(ts);
            }
        }
        List<TransferRoute> transferRouteList = new ArrayList<>();
        for (TransportStop ts: stopSet){
            List<DirectRoute> legFrom = legsBetween(stopFrom, ts, dayType);
            List<DirectRoute> legTo = legsBetween(ts, stopTo, dayType);
            for (DirectRoute directRouteFrom: legFrom){
                for (DirectRoute directRouteTo: legTo){
                    TransferRoute transferRoute = new TransferRoute(directRouteFrom, directRouteTo);
                    if (transferRoute.feasibility()){
                        transferRouteList.addLast(transferRoute);
                    }
                }
            }
        }
        return List.copyOf(transferRouteList);
    }

    /**
     * Ищет поездку с пересадкой, на которую уйдет минимально возможное время.
     * @param stopFrom остановка от которой ведется поиск
     * @param stopTo остановка до которой ведется поиск
     * @param dayType тип дня
     * @return возвращает опционал от поездки с пересадкой. Пустой опционал означает отсутствие осуществимой поездки
     */
    public Optional<TransferRoute> transferRouteWithMinTime(TransportStop stopFrom, TransportStop stopTo, DayType dayType){
        List<TransferRoute> transferRouteList = transferRouteList(stopFrom, stopTo, dayType);
        return transferRouteList.stream().min(new Comparator<TransferRoute>() {
            @Override
            public int compare(TransferRoute o1, TransferRoute o2) {
                return o1.allTimeInJourney().compareTo(o2.allTimeInJourney());
            }
        });
    }

    /**
     * Возвращает неизменяемую копию списка.
     * @return неизменяемая копия списка
     */
    public List<Route> getRouteList() {
        return List.copyOf(routeList);
    }
}
