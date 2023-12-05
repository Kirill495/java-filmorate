package ru.yandex.practicum.filmorate.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ru.yandex.practicum.filmorate.dao.impl.EventDaoImpl;
import ru.yandex.practicum.filmorate.exceptions.user.UserDataValidationException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.RecommendationsService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Map;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    UserController controller;
    User user;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserStorage storage = new InMemoryUserStorage();
        UserService service = new UserService(storage, new EventService(new EventDaoImpl(new JdbcTemplate())));
        FilmStorage filmStorage = new InMemoryFilmStorage();
        RecommendationsService recommendationsService = new RecommendationsService(filmStorage);
        controller = new UserController(service, recommendationsService);

        user = new User();
        user.setName("Username");
        user.setLogin("UserLogin");
        user.setEmail("User@email.org");
        user.setBirthday(LocalDate.of(2000, 2, 2));
    }

    @Test
    @DisplayName("После успешного добавления пользователя," +
            "размер списка пользователей должен увеличиться на 1")
    void addUserShouldIncreaseUserListByOne() {
        int sizeBefore = controller.getUsers().size();
        controller.addUser(user);
        assertEquals(1, controller.getUsers().size() - sizeBefore);
    }

    @Test
    @DisplayName("Метод добавления пользователя должен вернуть того же самого пользователя, только" +
            "с заполненным id")
    void addUserShouldReturnTheSameUserWithId() {
        User returnedUser = controller.addUser(user);
        assertEquals(user.getLogin(), returnedUser.getLogin());
        assertEquals(user.getName(), returnedUser.getName());
        assertEquals(user.getBirthday(), returnedUser.getBirthday());
        assertEquals(user.getEmail(), returnedUser.getEmail());
        assertNotEquals(0, returnedUser.getId());
    }

    @Test
    @DisplayName("При добавлении пользователя с name==login, " +
            "на место имени должен подставиться логин")
    void addUserWithEmptyNameShouldSetLoginAsName() {
        user.setName(null);
        controller.addUser(user);
        assertSame(user.getLogin(), user.getName());
    }

    @Test
    @DisplayName("При добавлении пользователя с пустым именем, " +
            "на место имени должен подставиться логин")
    void addUserWithBlankNameShouldSetLoginAsName() {
        user.setName("   ");
        controller.addUser(user);
        assertSame(user.getLogin(), user.getName());
        assertEquals(1, controller.getUsers().size());
    }

    @Test
    void addUserWithEmptyLoginShouldThrowException() throws Exception {
        user.setLogin("");
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("login", "Логин не должен быть пустым или содержать пробелы"))));
    }

    @Test
    void addUserWithNullLoginShouldThrowException() throws Exception {
        user.setLogin(null);
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("login", "Логин не должен быть пустым"))));
    }

    @Test
    void addUserWithLoginWithWhitespacesShouldThrowException() throws Exception {
        user.setLogin("l o g i n");
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("login", "Логин не должен быть пустым или содержать пробелы"))));
    }

    @Test
    void addUserWithEmailWithout_a_ShouldThrowException() throws Exception {
        user.setEmail("email");
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("email", "это не e-mail"))));
    }

    @Test
    void addUserWithBlankEmailShouldThrowException() throws Exception {
        user.setEmail("  ");
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("email", "это не e-mail"))));
    }

    @Test
    void addUserWithNullEmailShouldThrowException() throws Exception {
        user.setEmail(null);
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("email", "e-mail не должен быть пустым"))));
    }

    @Test
    void addUserWithIncorrectBirthdayShouldThrowException() throws Exception {
        user.setBirthday(LocalDate.now().plusDays(1));
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(
                        objectMapper
                                .writeValueAsString(
                                        Map.of("birthday", "Дата рождения не может быть больше текущей даты"))));
    }

    @Test
    void updateUserShouldReturnUpdatedUserAndUpdateUserInStorage() {
        final int existingUserId = controller.addUser(user).getId();
        User newUser = new User();
        newUser.setId(existingUserId);
        newUser.setName("NewUserName");
        newUser.setEmail("newUser@email.org");
        newUser.setLogin("newUserLogin");

        User returnedUser = controller.updateUser(newUser);
        assertSame(newUser, returnedUser);
        assertSame(
                newUser,
                controller
                        .getUsers()
                        .stream()
                        .filter(u -> (u.getId() == existingUserId))
                        .findAny()
                        .orElse(null));

    }

    @Test
    void updateUserWithEmptyIdShouldThrowException() {
        controller.addUser(user);
        User newUser = new User();
        newUser.setName("NewUserName");
        newUser.setEmail("newUser@email.org");
        newUser.setLogin("newUserLogin");
        UserDataValidationException e = assertThrows(
                UserDataValidationException.class,
                () -> controller.updateUser(newUser));
        assertEquals("Идентификатор пользователя не может быть пустым", e.getMessage());
    }

    @Test
    void updateUserWithNonExistingIdShouldThrowException() {
        controller.addUser(user);
        User newUser = new User();
        newUser.setId(-100);
        newUser.setName("NewUserName");
        newUser.setEmail("newUser@email.org");
        newUser.setLogin("newUserLogin");
        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> controller.updateUser(newUser));
        assertEquals("Неизвестный идентификатор пользователя", e.getMessage());
    }
}