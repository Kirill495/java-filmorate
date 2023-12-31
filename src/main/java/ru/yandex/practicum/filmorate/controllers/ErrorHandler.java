package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmDataValidationException;
import ru.yandex.practicum.filmorate.exceptions.user.UserDataValidationException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleItemNotFoundException(final ItemNotFoundException exception) {
        log.debug("Получен статус {}", HttpStatus.NOT_FOUND, exception);
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleFilmDataValidationException(final FilmDataValidationException exception) {
        log.debug("Получен статус {}", HttpStatus.BAD_REQUEST, exception);
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUserDataValidationException(final UserDataValidationException exception) {
        log.debug("Получен статус {}", HttpStatus.BAD_REQUEST, exception);
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleConstraintViolationException(final ConstraintViolationException exception) {
        Map<String, String> error = new HashMap<>();
        log.debug("Получен статус {}", HttpStatus.BAD_REQUEST, exception);
        exception
                .getConstraintViolations()
                .forEach(violation -> error.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        Map<String, String> error = new HashMap<>();
        log.debug("Получен статус {}", HttpStatus.BAD_REQUEST, exception);
        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> error.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return error;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleCommonException(final RuntimeException exception) {
        log.debug("Получен статус {}", HttpStatus.INTERNAL_SERVER_ERROR, exception);
        return Map.of("error", exception.getMessage());
    }
}
