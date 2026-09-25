package ru.example;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Маршрут транспорта: упорядоченная последовательность остановок со временами хода между ними и набор рейсов по расписанию.
 */
public class Route {
    private final long routeCode;
    private final String routeName;
    private final TransportType transportType;
    private List<TransportStop> transportStopList = new ArrayList<>();
    private List<Segment> segmentList = new ArrayList<>();
    private List<Trip> tripList = new ArrayList<>();

    /**
     * Создает маршрут из одной остановки без участков.
     *
     * @param routeCode идентификатор, код маршрута
     * @param routeName имя маршрута
     * @param transportType тип транспорта, ходящего по маршруту
     * @param firstStop первая остановка маршрута
     * @throws IllegalArgumentException если код не положительный, имя пустое, тип транспорта или остановка null
     */
    public Route(long routeCode, String routeName, TransportType transportType, TransportStop firstStop) {
        if (routeCode <= 0){
            throw new IllegalArgumentException("route code must be positive");
        }
        if (routeName == null || routeName.isBlank()){
            throw new IllegalArgumentException("route name must be non-blank");
        }
        if (transportType == null){
            throw new IllegalArgumentException("transport type must not be null");
        }
        if (firstStop == null){
            throw new IllegalArgumentException("first stop must not be null");
        }
        this.routeCode = routeCode;
        this.routeName = routeName;
        this.transportType = transportType;
        transportStopList.addLast(firstStop);
    }

    /**
     * Добавление с конца новых остановок, сегментов.
     *
     * @param newTransportStop добавляемая остановка
     * @param time время от последней остановки до добавляемой
     * @throws IllegalArgumentException если остановка null или время null, нулевое или отрицательное
     */
    public void extensionTransportStop(TransportStop newTransportStop, Duration time){
        if (newTransportStop == null){
            throw new IllegalArgumentException("transport stop must not be null");
        }

        Segment newSegment = new Segment(transportStopList.getLast(), newTransportStop, time);
        segmentList.addLast(newSegment);
        transportStopList.addLast(newTransportStop);
    }

    /**
     * Добавление рейса в маршрут.
     *
     * @param newTrip добавляемый рейс
     * @throws IllegalArgumentException если рейс null
     * @throws ForeignTripException     если код маршрута рейса не совпадает с кодом этого маршрута
     * @throws DuplicateTripException   если рейс с теми же временем отправления и признаком дня уже есть в маршруте
     */
    public void addTrip(Trip newTrip){
        if (newTrip == null){
            throw new IllegalArgumentException("new trip must not be null");
        }
        if (newTrip.codeOfRoute() != routeCode){
            throw new ForeignTripException("trip code must be same with route code");
        }
        if (tripList.contains(newTrip)){
            throw new DuplicateTripException("new trip is duplicating with old trip");
        }
        tripList.addLast(newTrip);
    }

    /**
     * Расчётное время прибытия на k-ю остановку по порядковому номеру.
     *
     * @param trip рейс, который идет по маршруту
     * @param k порядковый номер остановки, до которой рассчитывается время
     * @return время прибытия на k-ю остановку
     * @throws ForeignTripException если рейс не входит в список рейсов маршрута
     * @throws RangeIndexException  если k вне диапазона остановок маршрута
     */
    public LocalTime arrivalKStop(Trip trip, int k){
        if (!tripList.contains(trip)){
            throw new ForeignTripException("trip must be in the trip list");
        }
        if (k < 0){
            throw new RangeIndexException("index k must not be negative");
        }
        if (k > transportStopList.size() - 1){
            throw new RangeIndexException("index k must be less than count of transport stops");
        }

        if (k == 0){
            return trip.departureTime();
        }
        else {
            LocalTime lt = trip.departureTime();
            for (int i = 0; i < k; i++){
                lt = lt.plus(segmentList.get(i).duration());
            }
            return lt;
        }
    }

    /**
     * Расчётное время прибытия на k-ю остановку по остановке.
     *
     * @param trip рейс, который идет по маршруту
     * @param transportStop остановка, до которой рассчитывается время
     * @return время прибытия на k-ю остановку
     * @throws IllegalArgumentException         если остановка null
     * @throws ForeignTripException             если рейс не входит в список рейсов маршрута
     * @throws TransportStopNotInRouteException если остановка не входит в последовательность остановок маршрута
     */
    public LocalTime arrivalNameStop(Trip trip, TransportStop transportStop){
        if (!tripList.contains(trip)){
            throw new ForeignTripException("trip must be in the trip list");
        }
        if (transportStop == null){
            throw new IllegalArgumentException("transport stop must not be null");
        }
        if (!transportStopList.contains(transportStop)){
            throw new TransportStopNotInRouteException("transport stop must be in transport stop list");
        }

        LocalTime lt = trip.departureTime();
        for (Segment sg: segmentList){
            if (sg.stopFrom().equals(transportStop)){
                break;
            } else {
                lt = lt.plus(sg.duration());
            }
        }
        return lt;
    }

    /**
     * Время всего маршрута.
     *
     * @return сумма всех времен участков
     */
    public Duration timeAllRoute(){
        Duration dt = Duration.ZERO;
        for (Segment sg: segmentList){
            dt = dt.plus(sg.duration());
        }
        return dt;
    }

    private List<Trip> getTripsThisDay(DayType dayType){
        List<Trip> tripsThisDay = new ArrayList<>();
        for (Trip t: tripList){
            if (t.dayType().equals(dayType)){
                tripsThisDay.addLast(t);
            }
        }
        return tripsThisDay;
    }

    /**
     * Интервалы между транспортом в определенный день.
     *
     * @param dayType тип дня (будний/выходной)
     * @return список интервалов между соседними отправлениями
     * @throws LessTripException если рейсов указанного дня меньше двух
     */
    public List<Duration> intervals(DayType dayType){
        List<Trip> tripThisDay = getTripsThisDay(dayType);

        if (tripThisDay.size() < 2){
            throw new LessTripException("count of trip must be >= 2");
        }

        tripThisDay.sort(new Comparator<Trip>() {
            @Override
            public int compare(Trip o1, Trip o2) {
                return o1.departureTime().compareTo(o2.departureTime());
            }
        });
        List<Duration> durationList = new ArrayList<>();
        for (int i = 1; i < tripThisDay.size(); i++){
            Duration duration = Duration.between(tripThisDay.get(i - 1).departureTime(), tripThisDay.get(i).departureTime());
            durationList.addLast(duration);
        }
        return List.copyOf(durationList);
    }

    /**
     * Возвращает остановки маршрута по порядку.
     *
     * @return неизменяемая копия списка остановок
     */
    public List<TransportStop> getTransportStopList(){
        return List.copyOf(transportStopList);
    }

    /**
     * Возвращает участки маршрута по порядку.
     *
     * @return неизменяемая копия списка участков
     */
    public List<Segment> getSegmentList(){
        return List.copyOf(segmentList);
    }

    /**
     * Возвращает все рейсы маршрута.
     *
     * @return неизменяемая копия списка рейсов
     */
    public List<Trip> getTripList(){
        return List.copyOf(tripList);
    }

    /**
     * Возвращает рейсы маршрута с указанным признаком дня.
     *
     * @param dayType признак дня, по которому отбираются рейсы
     * @return неизменяемая копия списка рейсов дня в порядке добавления
     */
    public List<Trip> getTripsListThisDay(DayType dayType){
        return List.copyOf(getTripsThisDay(dayType));
    }

    /**
     * Возвращает уникальный код маршрута.
     *
     * @return код маршрута
     */
    public long getRouteCode() {
        return routeCode;
    }

    /**
     * Возвращает номер или имя маршрута.
     *
     * @return номер или имя маршрута
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * Возвращает тип транспорта маршрута.
     *
     * @return тип транспорта
     */
    public TransportType getTransportType() {
        return transportType;
    }
}
