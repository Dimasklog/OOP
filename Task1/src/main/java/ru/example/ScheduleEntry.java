package ru.example;

import java.time.LocalTime;

/**
 * Запись расписания.
 * @param route маршрут, для которого работает расписание
 * @param trip рейс, для которого работает расписание
 * @param arrivalTime время прибытия на остановку
 * @throws IllegalArgumentException если route, trip или arrivalTime null
 */
public record ScheduleEntry(Route route, Trip trip, LocalTime arrivalTime) {
    public ScheduleEntry{
        if (route == null){
            throw new IllegalArgumentException("route must not be null");
        }
        if (trip == null){
            throw new IllegalArgumentException("trip must not be null");
        }
        if (arrivalTime == null){
            throw new IllegalArgumentException("arrivalTime must not be null");
        }
    }
}
