package ru.yandex.practicum.filmorate.exceptions.film;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class FilmNotFoundException extends ItemNotFoundException {
  public FilmNotFoundException(int filmId) {
    this(String.format("Фильм с id %d не найден", filmId));
  }

  public FilmNotFoundException(String message) {
    super(message);
  }
}
