package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {
  @Autowired
  private final FilmStorage storage;

  public FilmService(FilmStorage storage) {
    this.storage = storage;
  }

  public Film getFilm(int id) {
    Film film = storage.getFilm(id);
    if (film == null) {
      throw new FilmNotFoundException(id);
    }
    return film;
  }

  public Film addFilm(Film film) {
    return storage.addFilm(film);
  }

  public Film updateFilm(Film film) {
    return storage.updateFilm(film);
  }
  public List<Film> getFilms() {
    return storage.getFilms();
  }
}
