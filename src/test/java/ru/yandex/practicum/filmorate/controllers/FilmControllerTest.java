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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
@Transactional
@SpringBootTest
class FilmControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilmController controller;
    private Film film;
    private final MPA mpa = new MPA(1, "G", "Нет возрастных ограничений");

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("ААА");
        film.setDuration(100);
        film.setDescription("");
        film.setMpa(mpa);
        film.setReleaseDate(LocalDate.now());
    }

    @Test
    void createCorrectFilmShouldReturnTheSameFilmWithIDAndSaveFilmToStorage() {
        Film returnedFilm = controller.addFilm(film);
        Assertions.assertEquals(film.getName(), returnedFilm.getName());
        Assertions.assertEquals(film.getDuration(), returnedFilm.getDuration());
        Assertions.assertEquals(film.getReleaseDate(), returnedFilm.getReleaseDate());
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
        newFilm.setMpa(mpa);
        Film returnedFilm = controller.updateFilm(newFilm);
        Assertions.assertEquals(newFilm.getName(), returnedFilm.getName());
        Assertions.assertEquals(newFilm.getDescription(), returnedFilm.getDescription());
        Assertions.assertEquals(newFilm.getName(), controller.getFilms().get(0).getName());
        Assertions.assertEquals(newFilm.getDescription(), controller.getFilms().get(0).getDescription());
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
