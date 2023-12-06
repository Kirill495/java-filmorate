package ru.yandex.practicum.filmorate.exceptions.film;

public class IncorrectSearchFilmParameterException extends RuntimeException {
    public IncorrectSearchFilmParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectSearchFilmParameterException(Throwable cause) {
        super(cause);
    }

    public IncorrectSearchFilmParameterException(String message) {
        super(message);
    }
}
