package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.FilmDataValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.FilmValidator;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
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
    if (filmId == 0) {
      throw new FilmDataValidationException("Идентификатор фильма не может быть пустым");
    }
    if (!films.containsKey(film.getId())) {
      throw new FilmDataValidationException("Неизвестный идентификатор фильма");
    }
    Validator validator = new FilmValidator(film);
    validator.validate();
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
  public Film getFilm(int id) {
    log.trace("Storage. Получение фильма по идентификатору {}", id);
    return films.get(id);
  }
}
