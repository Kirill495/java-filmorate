package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exceptions.FilmDataValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.validators.FilmValidator;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

  private final Map<Integer, Film> films = new HashMap<>();

  @PostMapping
  public Film addFilm(@RequestBody Film film) {
    log.debug("add new film {}", film);
    Validator validator = new FilmValidator(film);
    validator.validate();

    int maxId = films.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    film.setId(maxId + 1);
    films.put(film.getId(), film);
    log.debug("film is added successfully");
    return film;
  }

  @PutMapping
  public Film updateFilm(@RequestBody Film film) {
    log.debug("update film {}", film);

    if (film.getId() == 0) {
      throw new FilmDataValidationException("Идентификатор фильма не может быть пустым");
    }
    if (!films.containsKey(film.getId())) {
      throw new FilmDataValidationException("Неизвестный идентификатор фильма");
    }
    Validator validator = new FilmValidator(film);
    validator.validate();
    films.put(film.getId(), film);
    log.debug("film updated successfully");
    return film;
  }

  @GetMapping
  public List<Film> getFilms() {
    log.trace("get film data");
    return new ArrayList<>(films.values());
  }
}
