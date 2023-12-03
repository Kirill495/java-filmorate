package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int id);

    List<Film> getTheMostPopularFilms(int count);

    List<Film> getMostPopularFilmsFilterByYearAndGenre(int limit, int genreId, int year);

    List<Film> getMostPopularFilmsFilterByGenre(int limit, int genreId);

    List<Film> getMostPopularFilmsFilterByYear(int limit, int year);
}
