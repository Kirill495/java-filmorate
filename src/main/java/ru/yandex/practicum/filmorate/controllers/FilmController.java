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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/film")
public class FilmController {

  private static final int MAX_DESCRIPTION_LENGTH = 200;
  private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);
  Map<Integer, Film> films = new HashMap<>();

  @PostMapping
  public Film addFilm(@RequestBody Film film) {
    log.debug("add new film {}", film);
    filmDataValidation(film);
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

    films.put(film.getId(), film);
    log.debug("film updated successfully");
    return film;
  }

  @GetMapping
  public List<Film> getFilms(){
    log.trace("get film data");
    return new ArrayList<>(films.values());
  }

  private static void filmDataValidation(Film film) {

    if (film.getName() == null || film.getName().isBlank()) {
      throw new FilmDataValidationException("Название фильма не может быть пустым");
    }
    if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
      throw new FilmDataValidationException("Описание фильма не может превышать 200 символов");
    }
    if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
      throw new FilmDataValidationException(String.format("Дата релиза фильма должна быть ранее %s", MIN_RELEASE_DATE));
    }
    if (film.getDuration() <= 0) {
      throw new FilmDataValidationException("Продолжительность фильма должна быть положительной");
    }
  }
}
