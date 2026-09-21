package ru.example;

/**
 * Типы транспорта, которые ходят по маршрутам.
 *
 * Каждый тип транспорта хранит его скорость и вместимость.
 */
public enum TransportType {
    /** Автобус, скорость 60 км/ч, вместимость 54 человека. */
    BUS(60, 54),
    /** Трамвай, скорость 40 км/ч, вместимость 45 человека. */
    TRAM(40, 45),
    /** Троллейбус, скорость 60 км/ч, вместимость 65 человека. */
    TROLLEY(60, 65),
    /** Метро, скорость 120 км/ч, вместимость 140 человека. */
    METRO(120, 140);

    private final int speed;
    private final int capacity;

    TransportType(int speed, int capacity) {
        this.speed = speed;
        this.capacity = capacity;
    }

    /**
     * Возвращает скорость типа транспорта в км/ч.
     * @return скорость данного типа транспорта в км/ч
     */
    public int getSpeed(){
        return this.speed;
    }

    /**
     * Возвращает вместимость типа транспорта в пассажирах.
     * @return вместимость данного типа транспорта в пассажирах
     */
    public int getCapacity(){
        return this.capacity;
    }

}
