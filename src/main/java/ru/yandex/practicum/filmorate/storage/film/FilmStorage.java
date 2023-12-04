package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int id);

    List<Film> getTheMostPopularFilms(int count);

    List<Film> getSortedFilms(int id, String sortBy);

    List<Film> getFilmsBySearchParameters(String query, Set<String> queryParameters);
}
