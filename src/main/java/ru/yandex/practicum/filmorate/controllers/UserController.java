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
import ru.yandex.practicum.filmorate.validators.UserValidator;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

  private final Map<Integer, User> users = new HashMap<>();

  @PostMapping
  public User addUser(@RequestBody User user) {
    log.debug("create new user: {}", user);
    Validator dataValidator = new UserValidator(user);
    dataValidator.validate();
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
    Validator dataValidator = new UserValidator(user);
    dataValidator.validate();
    users.put(user.getId(), user);
    return user;
  }

  @GetMapping
  public List<User> getUsers() {
    log.trace("get all users");
    return new ArrayList<>(users.values());
  }

}
