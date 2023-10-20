package ru.yandex.practicum.filmorate.exceptions.film;

public class FilmNotFoundException extends RuntimeException {
  public FilmNotFoundException(int filmId) {
    this(String.format("Фильм с id %d не найден", filmId));
  }

  public FilmNotFoundException(String message) {
    super(message);
  }
}
