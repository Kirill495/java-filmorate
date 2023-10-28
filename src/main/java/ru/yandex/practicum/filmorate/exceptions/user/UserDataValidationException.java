package ru.yandex.practicum.filmorate.exceptions.user;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

public class UserDataValidationException extends ValidationException {
  public UserDataValidationException() {
    this("Некорректные данные пользователя.");
  }

  public UserDataValidationException(String message) {
    super(message);
  }
}
