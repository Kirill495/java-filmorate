package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {

  private final FilmStorage storage;
  private final UserService userService;

  @Autowired
  public FilmService(FilmStorage storage, UserService userService) {
    this.storage = storage;
    this.userService = userService;
  }

  public Film getFilm(int id) {
    return getFilmInner(id);
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

  public boolean addLikeFilm(int filmId, int userId) {
    Film film = getFilmInner(filmId);
    userService.getUser(userId);
    Set<Integer> likes = film.getLikes();
    if (!likes.contains(userId)) {
      film.getLikes().add(userId);
      storage.updateFilm(film);
      return true;
    }
    return false;
  }

  public boolean removeLikeFromFilm(int filmId, int userId) {
    Film film = getFilmInner(filmId);
    userService.getUser(userId);
    Set<Integer> likes = film.getLikes();
    if (likes.contains(userId)) {
      likes.remove(userId);
      storage.updateFilm(film);
      return true;
    }
    return false;
  }

  public List<Film> getTheMostPopularFilms(int count) {
    return storage.getFilms()
            .stream()
            .sorted(Comparator.comparingInt(film -> -film.getLikes().size()))
            .limit(count)
            .collect(Collectors.toList());
  }

  private Film getFilmInner(int id) {
    Film film = storage.getFilm(id);
    if (film == null) {
      throw new FilmNotFoundException(id);
    }
    return film;
  }
}
