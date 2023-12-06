package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ru.yandex.practicum.filmorate.dao.impl.DirectorDaoImpl;
import ru.yandex.practicum.filmorate.dao.impl.FeedDaoImpl;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
@SpringBootTest
class FilmControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        UserService userService = new UserService(userStorage, new FeedService(new FeedDaoImpl(new JdbcTemplate())));
        DirectorDaoImpl directorStorage = new DirectorDaoImpl(new JdbcTemplate());
        DirectorService directorService = new DirectorService(directorStorage);
        FeedService feedService = new FeedService(new FeedDaoImpl(new JdbcTemplate()));
        FilmService filmService = new FilmService(filmStorage, userService, directorService, feedService);
        controller = new FilmController(filmService);

    }

    @Test
    void createCorrectFilmShouldReturnTheSameFilmWithIDAndSaveFilmToStorage() {
        Film returnedFilm = controller.addFilm(film);
        Assertions.assertSame(film.getName(), returnedFilm.getName());
        Assertions.assertSame(film.getDuration(), returnedFilm.getDuration());
        Assertions.assertSame(film.getReleaseDate(), returnedFilm.getReleaseDate());
        Assertions.assertNotEquals(0, returnedFilm.getId());
        List<Film> films = controller.getFilms();
        Assertions.assertEquals(returnedFilm, films.get(films.size() - 1));
    }

    @Test
    void createFilmWithBlankNameShouldFail() throws Exception {
        film.setName("");
        String body = objectMapper.writeValueAsString(film);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("name", "Название фильма не может быть пустым"))));
    }


    @Test
    void createFilmWithTooLongDescriptionShouldThrowException() throws Exception {
        String description = "____________________";
        film.setDescription(description.repeat(15));
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film));
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("description", "Описание фильма не может превышать 200 символов"))));
    }

    @Test
    void createFilmWithTooEarlyReleaseDateShouldThrowException() throws Exception {
        film.setReleaseDate(LocalDate.of(1700, 1, 1));
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film));
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("releaseDate", "Дата релиза должна быть позже 1895-12-28"))));
    }

    @Test
    void createFilmWithZeroDurationShouldThrowException() throws Exception {
        film.setDuration(0);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film));
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("duration", "Продолжительность фильма должна быть положительной"))));

    }

    @Test
    void createFilmWithNegativeDurationShouldThrowException() throws Exception {
        film.setDuration(-100);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film));
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("duration", "Продолжительность фильма должна быть положительной"))));
    }

    @Test
    void getFilmsMethodShouldReturnListOfFilms() {
        List<Film> filmsBefore = controller.getFilms();
        Assertions.assertTrue(filmsBefore.isEmpty());
        controller.addFilm(film);
        List<Film> filmsAfter = controller.getFilms();
        Assertions.assertEquals(1, filmsAfter.size() - filmsBefore.size());
    }

    @Test
    void updateFilmShouldChangeFilmInfo() {
        int filmId = controller.addFilm(film).getId();
        Film newFilm = new Film();
        newFilm.setId(filmId);
        newFilm.setName("updateFilmShouldChangeFilmInfo");
        newFilm.setDescription("new film description");
        newFilm.setDuration(3600);
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        Film returnedFilm = controller.updateFilm(newFilm);
        Assertions.assertEquals(newFilm, returnedFilm);
        Assertions.assertEquals(newFilm, controller.getFilms().get(0));
    }

    @Test
    void updateFilmWithNonExistingIdShouldThrowException() throws Exception {
        film.setId(9999);
        mvc.perform(
                        MockMvcRequestBuilders
                                .put("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(
                                Map.of("error", "Фильм с id 9999 не найден"))));
    }

    @Test
    void updateFilmWithZeroIdShouldThrowException() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(film));
        mvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("error", "Идентификатор фильма не может быть пустым"))));
    }
}
