package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilms();

    Film getFilm(int id);

    List<Film> getTheMostPopularFilms(int count);

    List<Film> getRecommendations(int userId);
}
