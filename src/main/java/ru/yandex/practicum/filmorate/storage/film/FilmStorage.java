package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int id);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getRecommendations(int userId);

    List<Film> getSortedFilms(int id, String sortBy);

    List<Film> getMostPopularFilmsFilterAll(Integer limit, Integer genreId, Integer year);
}
