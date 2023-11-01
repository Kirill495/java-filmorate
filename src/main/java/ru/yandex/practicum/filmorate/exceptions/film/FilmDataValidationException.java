package ru.yandex.practicum.filmorate.exceptions.film;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;

public class FilmDataValidationException extends ValidationException {
    public FilmDataValidationException() {
        this("Некорректные данные фильма");
    }

    public FilmDataValidationException(String message) {
        super(message);
    }
}
