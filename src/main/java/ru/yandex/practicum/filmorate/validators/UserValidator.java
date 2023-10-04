package ru.yandex.practicum.filmorate.validators;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.UserDataValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator implements Validator{
  User user;

  public UserValidator(User user) {
    this.user = user;
  }

  @Override
  public void validate() {
    log.trace("user validation is started: {}", user);
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
    log.trace("user validation is completed");
  }
}
