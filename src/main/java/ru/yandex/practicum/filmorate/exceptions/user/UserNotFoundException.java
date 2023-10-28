package ru.yandex.practicum.filmorate.exceptions.user;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(int id) {
    this(String.format("Пользователь с id %d не найден.", id));
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}
