package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.filmorate.exceptions.UserDataValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

  private static final Map<Integer, User> users = new HashMap<>();

  @PostMapping
  public User addUser(@RequestBody User user) {
    log.debug("create new user: {}", user);
    validateUserData(user);
    int maxId = users.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    user.setId(maxId + 1);
    users.put(user.getId(), user);
    return user;
  }

  @PutMapping
  public User updateUser(@RequestBody User user) {
    log.debug("update user {}", user);
    if (user.getId() == 0) {
      throw new UserDataValidationException("Идентификатор пользователя не может быть пустым");
    }
    if (!users.containsKey(user.getId())) {
      throw new UserDataValidationException("Неизвестный идентификатор пользователя");
    }

    validateUserData(user);
    users.put(user.getId(), user);
    return user;
  }

  @GetMapping
  public List<User> getUsers() {
    log.trace("get all users");
    return new ArrayList<>(users.values());
  }

  private static void validateUserData(User user) {

    if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
      throw new UserDataValidationException("Email пустой или не содержит \"@\"");
    }
    if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
      throw new UserDataValidationException("Логин не может быть пустым или содержать пробелы");
    }
    if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
      throw new UserDataValidationException("Дата рождения не может быть больше текущей даты");
    }
    if (user.getName() == null || user.getName().isBlank()) {
      user.setName(user.getLogin());
    }
  }
}
