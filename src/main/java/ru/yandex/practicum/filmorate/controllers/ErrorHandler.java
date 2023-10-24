package ru.yandex.practicum.filmorate.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
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

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleConstraintViolationException(final ConstraintViolationException exception) {
    Map<String, String> error = new HashMap<>();
    exception
            .getConstraintViolations()
            .forEach(violation -> error.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return error;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
    Map<String, String> error = new HashMap<>();
    exception
            .getBindingResult()
            .getFieldErrors()
            .forEach(fieldError -> error.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return error;
  }

}
