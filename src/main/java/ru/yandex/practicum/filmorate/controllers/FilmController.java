package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
import ru.yandex.practicum.filmorate.validators.filmSearchParameter.FilmSearchParam;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService service;

    @Autowired
    public FilmController(FilmService service) {
        this.service = service;
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Adding film with id:{}", film.getId());
        return service.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Updating film with id:{}", film.getId());
        return service.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        log.info("Getting films");
        return service.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") @Positive(message = "{FilmId.Positive}") int id) {
        log.info("Getting film with id:{}", id);
        return service.getFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public boolean addLikeFilm(@PathVariable("id") @Positive(message = "{FilmId.Positive}") int filmId,
                               @PathVariable @Positive(message = "{UserId.Positive}") int userId) {
        log.info("Adding like to film with id:{} and user id:{}", filmId, userId);
        return service.addLikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public boolean removeLikeFilm(@PathVariable("id") @Positive(message = "{FilmId.Positive}") int filmId,
                                  @PathVariable int userId) {
        log.info("Removing like from film with id:{} and user id:{}", filmId, userId);
        return service.removeLikeFromFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopPopular(@RequestParam(defaultValue = "10") @Positive(message = "{Limit.Positive}") Integer count,
                                    @RequestParam(required = false) Integer genreId,
                                    @RequestParam(required = false)
                                    @Min(value = 1895, message = "Год выхода фильма не может быть меньше 1895") Integer year) {
        log.info("Getting top {} popular films with genre id:{} and year:{}", count, genreId, year);
        return service.getMostPopularFilms(count, genreId, year);
    }

    @DeleteMapping("/{filmId}")
    public boolean deleteFilm(@PathVariable @Positive(message = "{FilmId.Positive}") int filmId) {
        log.info("Delete film with id:{}", filmId);
        return service.deleteFilm(filmId);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam @Positive(message = "{UserId.Positive}") int userId,
                                     @RequestParam @Positive(message = "{UserId.Positive}") int friendId) {
        log.info("Getting common films from user with id:{} and friend id:{}", userId, friendId);
        return service.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilms(@PathVariable("directorId") @Positive(message = "{DirectorId.Positive}") int id,
                                     @RequestParam String sortBy) {
        log.info("Getting sorted films with director id:{} and sorted by:{}", id, sortBy);
        return service.getSortedFilms(id, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(@RequestParam String query, @RequestParam("by") @FilmSearchParam String filter) {
        log.info("Searching films with query:\"{}\" and filter:\"{}\"", query, filter);
        return service.searchFilms(query, filter);
    }
}
