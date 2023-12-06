package ru.yandex.practicum.filmorate.exceptions.film;

public class IncorrectSearchFilmParameterException extends RuntimeException {
    public IncorrectSearchFilmParameterException() {
        super("Параметр запроса \"by\" должен содержать значения: DIRECTOR или TITLE");
    }
}
