    package ru.example;

    import java.time.LocalTime;

    /**
     * Рейс по маршруту.
     * @param codeOfRoute идентификатор, уникальный код маршрута
     * @param departureTime время отправления рейса
     * @param dayType тип дня (будний или выходной)
     */
    public record Trip(long codeOfRoute, LocalTime departureTime, DayType dayType) {
        public Trip{
            if (codeOfRoute <= 0){
                throw new IllegalArgumentException("codeOfRoute must be positive");
            }
            if (departureTime == null){
                throw new IllegalArgumentException("departure time must not be null");
            }
            if (dayType == null){
                throw new IllegalArgumentException("day type must not be null");
            }
        }
    }
