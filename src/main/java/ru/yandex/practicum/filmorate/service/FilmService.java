package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmDataValidationException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.film.IncorrectSearchFilmParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Set;

@Service
public class FilmService {

    private final FilmStorage storage;
    private final UserService userService;
    private final DirectorService directorService;
    private final FeedService feedService;
    private static final String SEARCH_PARAM_TITLE = "TITLE";
    private static final String SEARCH_PARAM_DIRECTOR = "DIRECTOR";

    @Autowired
    public FilmService(@Qualifier("FilmDbStorage") FilmStorage storage, UserService userService, DirectorService directorService, FeedService feedService) {
        this.storage = storage;
        this.userService = userService;
        this.directorService = directorService;
        this.feedService = feedService;
    }

    public Film getFilm(int id) {
        return getFilmInner(id);
    }

    public Film addFilm(Film film) {
        return storage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        int filmId = film.getId();
        if (filmId == 0) {
            throw new FilmDataValidationException("Идентификатор фильма не может быть пустым");
        }
        return storage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return storage.getFilms();
    }

    public boolean addLikeFilm(int filmId, int userId) {
        Film film = getFilmInner(filmId);
        userService.getUser(userId);
        Set<Integer> likes = film.getLikes();
        feedService.postEvent(userId, film, Operation.ADD);
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
        feedService.postEvent(userId, film, Operation.REMOVE);
        if (likes.contains(userId)) {
            likes.remove(userId);
            storage.updateFilm(film);
            return true;
        }
        return false;
    }

    public List<Film> searchFilms(String query, String filter) {
        Set<String> filterParams = Set.of(filter.toUpperCase().split(","));
        if (!filterParams.contains(SEARCH_PARAM_TITLE) && !filterParams.contains(SEARCH_PARAM_DIRECTOR)) {
            throw new IncorrectSearchFilmParameterException(
                "Параметр запроса \"by\" должен содержать значения: DIRECTOR или TITLE");
        }
        return storage.getFilmsBySearchParameters(query, filterParams);
    }

    private Film getFilmInner(int id) {
        Film film = storage.getFilm(id);
        if (film == null) {
            throw new FilmNotFoundException(id);
        }
        return film;
    }

    public boolean deleteFilm(int filmId) {
        getFilmInner(filmId);
        return storage.deleteFilm(filmId);
    }

    public List<Film> getMostPopularFilms(Integer limit, Integer genreId, Integer year) {
        return storage.getMostPopularFilmsFilterAll(limit, genreId, year);
    }

    public List<Film> getSortedFilms(int id, String sortBy) {
        directorService.getDirectorById(id);
        return storage.getSortedFilms(id, sortBy);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return storage.getCommonFilms(userId, friendId);
    }
}
