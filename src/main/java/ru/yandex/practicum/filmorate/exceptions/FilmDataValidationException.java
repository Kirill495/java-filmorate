package ru.yandex.practicum.filmorate.exceptions;

public class FilmDataValidationException extends ValidationException{
  public FilmDataValidationException() {
    this("Некорректные данные фильма");
  }

  public FilmDataValidationException(String message) {
    super(message);
  }
}
