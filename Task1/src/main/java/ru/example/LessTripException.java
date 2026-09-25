package ru.example;

/**
 * Исключение недостатка рейсов: рейсов указанного дня меньше двух, интервалы не определены.
 */
public class LessTripException extends RuntimeException {
  /**
   * Создаёт исключение с пояснением причины.
   *
   * @param message пояснение причины
   */
  public LessTripException(String message) {
    super(message);
  }
}
