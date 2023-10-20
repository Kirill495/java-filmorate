package ru.yandex.practicum.filmorate.controllers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.exceptions.FilmDataValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

class FilmControllerTest {

  private FilmController controller;
  private Film film;

  @BeforeEach
  void setUp() {
    film = new Film();
    film.setName("ААА");
    film.setDuration(100);
    film.setDescription("");
    film.setReleaseDate(LocalDate.now());
    FilmStorage filmStorage = new InMemoryFilmStorage();
    UserStorage userStorage = new InMemoryUserStorage();
    UserService userService = new UserService(userStorage);
    FilmService filmService = new FilmService(filmStorage, userService);
    controller = new FilmController(filmService);

  }

  @Test
  void createCorrectFilmShouldReturnTheSameFilmWithIDAndSaveFilmToStorage() {
    Film returnedFilm = controller.addFilm(film);
    assertSame(film.getName(), returnedFilm.getName());
    assertSame(film.getDuration(), returnedFilm.getDuration());
    assertSame(film.getReleaseDate(), returnedFilm.getReleaseDate());
    assertNotEquals(0, returnedFilm.getId());
    List<Film> films = controller.getFilms();
    assertEquals(returnedFilm, films.get(films.size() - 1));
  }

  @Test
  void createFilmWithBlankNameShouldFail() {
    film.setName("");
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.addFilm(film));
    assertEquals("Название фильма не может быть пустым", e.getMessage());
  }

  @Test
  void createFilmWithTooLongDescriptionShouldThrowException() {
    String description = "____________________";
    StringBuilder filmDescription = new StringBuilder();
    filmDescription.append(description.repeat(15));
    film.setDescription(filmDescription.toString());
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.addFilm(film));
    assertEquals("Описание фильма не может превышать 200 символов", e.getMessage());
  }

  @Test
  void createFilmWithTooEarlyReleaseDateShouldThrowException() {
    film.setReleaseDate(LocalDate.of(1700,1, 1));
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.addFilm(film));
    assertEquals(
            String.format("Дата релиза фильма должна быть ранее %s",
                    LocalDate.of(1895, 12, 28)),
            e.getMessage());
  }

  @Test
  void createFilmWithZeroDurationShouldThrowException() {
    film.setDuration(0);
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.addFilm(film));
    assertEquals(
            "Продолжительность фильма должна быть положительной",
            e.getMessage());
  }

  @Test
  void createFilmWithNegativeDurationShouldThrowException() {
    film.setDuration(-100);
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.addFilm(film));
    assertEquals(
            "Продолжительность фильма должна быть положительной",
            e.getMessage());
  }

  @Test
  void getFilmsMethodShouldReturnListOfFilms() {
    List<Film> filmsBefore = controller.getFilms();
    assertTrue(filmsBefore.isEmpty());
    controller.addFilm(film);
    List<Film> filmsAfter = controller.getFilms();
    assertEquals(1, filmsAfter.size() - filmsBefore.size());
  }

  @Test
  void updateFilmShouldChangeFilmInfo() {
    int filmId = controller.addFilm(film).getId();
    Film newFilm = new Film();
    newFilm.setId(filmId);
    newFilm.setName("updateFilmShouldChangeFilmInfo");
    newFilm.setDescription("new film decription");
    newFilm.setDuration(3600);
    newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));

    Film returnedFilm = controller.updateFilm(newFilm);
    assertEquals(newFilm, returnedFilm);
    assertEquals(newFilm, controller.getFilms().get(0));
  }

  @Test
  void updateFilmWithNonExistingIdShouldThrowException() {
    film.setId(1);
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.updateFilm(film));
    assertEquals(
            "Неизвестный идентификатор фильма",
            e.getMessage());
  }

  @Test
  void updateFilmWithZeroIdShouldThrowException() {
    FilmDataValidationException e = assertThrows(
            FilmDataValidationException.class,
            () -> controller.updateFilm(film));
    assertEquals(
            "Идентификатор фильма не может быть пустым",
            e.getMessage());
  }
}
