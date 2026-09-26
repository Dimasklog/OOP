package ru.example;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

/**
 * Поездка без пересадок, прямая поездка.
 * @param route маршрут по которому осуществляется поездка
 * @param trip рейс на котором осуществляется поездка
 * @param landingTransportStop остановка посадки
 * @param leavingTransportStop остановка выхода
 * @throws IllegalArgumentException если route, landingTransportStop, leavingTransportStop или trip - null
 * @throws ForeignTripException если код trip не совпадает с кодом route
 * @throws TransportStopNotInRouteException если landingTransportStop или leavingTransportStop не содержатся в списке остановок у route
 * @throws RangeIndexException если landingTransportStop после leavingTransportStop
 */
public record DirectRoute(Route route, Trip trip, TransportStop landingTransportStop, TransportStop leavingTransportStop) implements Journey {
    public DirectRoute{
        if (route == null){
            throw new IllegalArgumentException("route must not be null");
        }
        if (landingTransportStop == null){
            throw new IllegalArgumentException("landingTransportStop must not be null");
        }
        if (leavingTransportStop == null){
            throw new IllegalArgumentException("leavingTransportStop must not be null");
        }
        if (trip == null){
            throw new IllegalArgumentException("trip must not be null");
        }
        if (trip.codeOfRoute() != route.getRouteCode()){
            throw new ForeignTripException("trip code must be same with route code");
        }
        if (!route.getTransportStopList().contains(landingTransportStop) || !route.getTransportStopList().contains(leavingTransportStop)){
            throw new TransportStopNotInRouteException("transport stops must be in route");
        }
        if (route.getTransportStopList().indexOf(landingTransportStop) >= route.getTransportStopList().indexOf(leavingTransportStop)){
            throw new RangeIndexException("landing stop must be before leaving stop");
        }
    }

    /**
     * Общее время поездки, сумма времени на участках между остановками захода и выхода.
     * @return общее время поездки
     */
    @Override
    public Duration allTimeInJourney() {
        Duration allTimeDuration = Duration.ZERO;
        int indexLand = route.getTransportStopList().indexOf(landingTransportStop);
        int indexLeave = route.getTransportStopList().indexOf(leavingTransportStop);
        List<Segment> lst = route.getSegmentList();
        for (int i = indexLand; i < indexLeave; i++){
            allTimeDuration = allTimeDuration.plus(lst.get(i).duration());
        }
        return allTimeDuration;
    }

    /**
     * Количество пересадок
     * @return 0, пересадок нет
     */
    @Override
    public int countTransfer() {
        return 0;
    }

    /**
     * Осуществление поездки.
     * @return поездка может быть осуществлена
     */
    @Override
    public boolean feasibility() {
        return true;
    }
}
