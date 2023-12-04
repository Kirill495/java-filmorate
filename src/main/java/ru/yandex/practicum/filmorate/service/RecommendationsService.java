
package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class RecommendationsService {

    private final FilmStorage storage;

    @Autowired
    public RecommendationsService(@Qualifier("FilmDbStorage") FilmStorage storage) {
        this.storage = storage;
    }

    public List<Film> getRecommendations(int userId) {
        return storage.getRecommendations(userId);
    }

}
