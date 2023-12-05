package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Qualifier("InMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        log.debug("add new film {}", film);
        int maxId = films.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        film.setId(maxId + 1);
        films.put(film.getId(), film);
        log.debug("film is added successfully");
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.debug("update film {}", film);
        int filmId = film.getId();
        if (!films.containsKey(filmId)) {
            throw new FilmNotFoundException(filmId);
        }
        films.put(filmId, film);
        log.debug("film updated successfully");
        return film;
    }

    @Override
    public List<Film> getFilms() {
        log.trace("get film data");
        return new ArrayList<>(films.values());
    }


    @Override
    public List<Film> getSortedFilms(int id, String sortBy) {
        return null;
    }

    @Override
    public Film getFilm(int id) {
        log.trace("Storage. Получение фильма по идентификатору {}", id);
        return films.get(id);
    }

    @Override
    public boolean deleteFilm(int filmId) {
        throw new UnsupportedOperationException(("Этот метод невозможно вызвать через InMemoryFilmStorage, " +
                "попробуйте через FilmDbStorage"));
    }

    @Override
    public List<Film> getMostPopularFilmsFilterAll(Integer limit, Integer genreId, Integer year) {
        throw new UnsupportedOperationException("Этот метод невозможно вызвать через InMemoryFilmStorage, " +
                "попробуйте через FilmDbStorage");
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        return null;
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        return new ArrayList<>();
    }
}
