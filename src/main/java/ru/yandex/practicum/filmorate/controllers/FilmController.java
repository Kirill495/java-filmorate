package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validators.FilmValidator;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

  @Autowired
  private final FilmService service;

  public FilmController(FilmService service) {
    this.service = service;
  }

  @PostMapping
  public Film addFilm(@RequestBody Film film) {
    Validator validator = new FilmValidator(film);
    validator.validate();
    return service.addFilm(film);
  }

  @PutMapping
  public Film updateFilm(@RequestBody Film film) {
    return service.updateFilm(film);
  }

  @GetMapping
  public List<Film> getFilms() {
    return service.getFilms();
  }

  @GetMapping("/{id}")
  public Film getFilm(@PathVariable("id") int id) {
    return service.getFilm(id);
  }

  @PutMapping("/{id}/like/{userId}")
  public boolean addLikeToTheFilm(@PathVariable("id") int filmId, @PathVariable int userId) {
    return service.addLikeToTheFilm(filmId, userId);
  }

  @DeleteMapping("/{id}/like/{userId}")
  public boolean removeLikeFromTheFilm(@PathVariable("id") int filmId, @PathVariable int userId) {
    return service.removeLikeFromTheFilm(filmId, userId);
  }

  @GetMapping("/popular?count={count}")
  public List<Film> getTopPopular(@RequestParam(defaultValue = "10") int count) {
    return service.getTheMostPopularFilms(count);
  }
}
