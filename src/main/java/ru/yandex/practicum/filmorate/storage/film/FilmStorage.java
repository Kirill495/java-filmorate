package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int id);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getRecommendations(int userId);

    boolean deleteFilm(int filmId);

    List<Film> getMostPopularFilms(int count);

    List<Film> getSortedFilms(int id, String sortBy);

    List<Film> getMostPopularFilmsFilterAll(Integer limit, Integer genreId, Integer year);

    List<Film> getFilmsBySearchParameters(String query, Set<String> queryParameters);
}
