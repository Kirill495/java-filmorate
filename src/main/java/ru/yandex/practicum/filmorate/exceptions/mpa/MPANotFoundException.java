package ru.yandex.practicum.filmorate.exceptions.mpa;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class MPANotFoundException extends ItemNotFoundException {
  public MPANotFoundException(int id) {
    this(String.format("Рейтинг MPA с id %d не найден.", id));
  }

  public MPANotFoundException(String message) {
    super(message);
  }
}
