package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.FilmDataValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
public class FilmValidator implements Validator {

  private static final int MAX_DESCRIPTION_LENGTH = 200;
  private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

  private final Film film;

  public FilmValidator(Film film) {
    this.film = film;
  }

  @Override
  public void validate() {
    log.trace("film validation is started: {}", film);
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
    log.trace("film validation is completed");
  }
}
