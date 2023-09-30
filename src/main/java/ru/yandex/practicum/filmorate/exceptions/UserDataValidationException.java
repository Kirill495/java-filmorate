package ru.yandex.practicum.filmorate.exceptions;

public class UserDataValidationException extends ValidationException {
  public UserDataValidationException() {
    this("Некорректные данные пользователя.");
  }

  public UserDataValidationException(String message) {
    super(message);
  }
}
