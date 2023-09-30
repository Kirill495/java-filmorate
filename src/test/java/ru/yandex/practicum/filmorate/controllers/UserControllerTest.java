package ru.yandex.practicum.filmorate.controllers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.UserDataValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

class UserControllerTest {

  UserController controller;
  User user;

  @BeforeEach
  void setUp() {
    controller = new UserController();
    user = new User();
    user.setName("Username");
    user.setLogin("UserLogin");
    user.setEmail("User@email.org");
    user.setBirthday(LocalDate.of(2000, 2, 2));
  }

  @Test
  void addUserShouldIncreaseUserListByOne() {
    int sizeBefore = controller.getUsers().size();
    controller.addUser(user);
    assertEquals(1, controller.getUsers().size() - sizeBefore);
  }

  @Test
  void addUserShouldReturnTheSameUserWithId() {
    User returnedUser = controller.addUser(user);
    assertEquals(user.getLogin(), returnedUser.getLogin());
    assertEquals(user.getName(), returnedUser.getName());
    assertEquals(user.getBirthday(), returnedUser.getBirthday());
    assertEquals(user.getEmail(), returnedUser.getEmail());
    assertNotEquals(1, returnedUser.getId());
  }

  @Test
  void addUserWithEmptyNameShouldSetLoginAsName() {
    user.setName(null);
    controller.addUser(user);
    assertSame(user.getLogin(), user.getName());
  }
  @Test
  void addUserWithBlankNameShouldSetLoginAsName() {
    user.setName("   ");
    controller.addUser(user);
    assertSame(user.getLogin(), user.getName());
    assertEquals(1, controller.getUsers().size());
  }

  @Test
  void addUserWithEmptyLoginShouldThrowException() {
    user.setLogin("");
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Логин не может быть пустым или содержать пробелы", e.getMessage());
  }

  @Test
  void addUserWithNullLoginShouldThrowException() {
    user.setLogin(null);
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Логин не может быть пустым или содержать пробелы", e.getMessage());
  }

  @Test
  void addUserWithLoginWithWhitespacesShouldThrowException() {
    user.setLogin("l o g i n");
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Логин не может быть пустым или содержать пробелы", e.getMessage());
  }

  @Test
  void addUserWithEmailWithout_a_ShouldThrowException() {
    user.setEmail("email");
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Email пустой или не содержит \"@\"", e.getMessage());
  }

  @Test
  void addUserWithBlankEmailShouldThrowException() {
    user.setEmail("  ");
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Email пустой или не содержит \"@\"", e.getMessage());
  }

  @Test
  void addUserWithNullEmailShouldThrowException() {
    user.setEmail(null);
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Email пустой или не содержит \"@\"", e.getMessage());
  }

  @Test
  void addUserWithIncorrectBirthdayShouldThrowException() {
    user.setBirthday(LocalDate.now().plusDays(1));
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.addUser(user));
    assertSame("Дата рождения не может быть больше текущей даты", e.getMessage());
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
    UserDataValidationException e = assertThrows(
            UserDataValidationException.class,
            () -> controller.updateUser(newUser));
    assertEquals("Неизвестный идентификатор пользователя", e.getMessage());
  }
}