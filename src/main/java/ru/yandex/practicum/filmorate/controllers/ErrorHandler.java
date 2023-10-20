package ru.yandex.practicum.filmorate.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {
  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, String> handleFilmNotFoundException(final FilmNotFoundException exception) {
    return Map.of("error", exception.getMessage());
  }

  @ExceptionHandler
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, String> handleUserNotFoundException(final UserNotFoundException exception) {
    return Map.of("error", exception.getMessage());
  }
}
