package ru.yandex.practicum.filmorate.exceptions.user;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class UserNotFoundException extends ItemNotFoundException {
  public UserNotFoundException(int id) {
    this(String.format("Пользователь с id %d не найден.", id));
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}
