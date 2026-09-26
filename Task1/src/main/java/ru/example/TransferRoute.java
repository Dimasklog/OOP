package ru.example;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Поездка с пересадкой.
 * @param firstRoute первое плечо поездки, поездка до пересадки
 * @param secondRoute второе плечо поездки, поездка после пересадки
 * @throws IllegalArgumentException если firstRoute или secondRoute null, если остановка высадки для пересадки не совпадает с остановкой посадки после пересадки
 */
public record TransferRoute(DirectRoute firstRoute, DirectRoute secondRoute) implements Journey {
    public TransferRoute{
        if (firstRoute == null){
            throw new IllegalArgumentException("first route must not be null");
        }
        if (secondRoute == null){
            throw new IllegalArgumentException("second route must not be null");
        }
        if (!firstRoute.leavingTransportStop().equals(secondRoute.landingTransportStop())){
            throw new IllegalArgumentException("leaving transport stop on first route must be the same as the landing transport stop on second route");
        }
    }

    /**
     * Задержка пересадки.
     * @return сколько времени между рейсами, сколько есть на пересадку
     */
    public Duration transferGap(){
        LocalTime arrivalTime = firstRoute.route().arrivalNameStop(firstRoute.trip(), firstRoute.leavingTransportStop());
        LocalTime departureTime = secondRoute.route().arrivalNameStop(secondRoute.trip(), secondRoute.landingTransportStop());
        return Duration.between(arrivalTime, departureTime);
    }

    /**
     * Общее время пути.
     * @return сумма времен пути каждого плеча
     * @throws FeasibilityJourneyException если времени на пересадку недостаточно
     */
    @Override
    public Duration allTimeInJourney() {
        if (!feasibility()){
            throw new FeasibilityJourneyException("journey is not feasible");
        }
        return firstRoute.allTimeInJourney().plus(secondRoute.allTimeInJourney());
    }

    /**
     * Количество пересадок.
     * @return 1 пересадка
     */
    @Override
    public int countTransfer() {
        return 1;
    }

    /**
     * Возможность пересадки.
     * @return если между рейсами >= 1 минуты, пересадка возможна
     */
    @Override
    public boolean feasibility() {
        return transferGap().compareTo(Journey.timeTransfer) >= 0;
    }
}
