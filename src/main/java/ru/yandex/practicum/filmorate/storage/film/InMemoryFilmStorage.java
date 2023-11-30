package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Film> getTheMostPopularFilms(int count) {
        return getFilms()
                .stream()
                .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Film getFilm(int id) {
        log.trace("Storage. Получение фильма по идентификатору {}", id);
        return films.get(id);
    }

    @Override
    public boolean filmDelete(int filmId) {
        throw new UnsupportedOperationException();
    }
}
