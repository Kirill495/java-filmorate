package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserDataValidationException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validators.UserValidator;
import ru.yandex.practicum.filmorate.validators.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

  @Autowired
  private UserStorage storage;

  public User addUser(User user) {
    Validator dataValidator = new UserValidator(user);
    dataValidator.validate();
    return storage.addUser(user);
  }

  public User updateUser(User user) {
    Validator dataValidator = new UserValidator(user);
    dataValidator.validate();
    if (user.getId() == 0) {
      throw new UserDataValidationException("Идентификатор пользователя не может быть пустым");
    }

    return storage.updateUser(user);
  }

  public List<User> getUsers() {
    return storage.getUsers();
  }

  public User getUser(int id) {
    User user = storage.getUser(id);
    if (user == null) {
      throw new UserNotFoundException(id);
    }
    return user;
  }

  public void addFriend(int userId, int friendId) {
    User user = storage.getUser(userId);
    User friend = storage.getUser(friendId);
    if (user == null) {
      throw new UserNotFoundException(userId);
    }
    if (friend == null) {
      throw new UserNotFoundException(friendId);
    }
    if (!user.getFriends().contains(friendId)) {
      user.getFriends().add(friendId);
      storage.updateUser(user);
    }
    if (!friend.getFriends().contains(userId)) {
      friend.getFriends().add(userId);
      storage.updateUser(friend);
    }
  }

  public void deleteFriend(int userId, int friendId) {
    User user = getUserInner(userId);
    User friend = getUserInner(friendId);
    if (user.getFriends().contains(friendId)) {
      user.getFriends().remove(friendId);
      storage.updateUser(user);
    }
    if (friend.getFriends().contains(userId)) {
      friend.getFriends().remove(userId);
      storage.updateUser(friend);
    }
  }
  public List<User> getFriends(int id) {
    User user = getUserInner(id);
    return user.getFriends().stream()
            .map(storage::getUser)
            .collect(Collectors.toList());
  }
  public List<User> getCommonFriends(int id, int otherId) {
    User mainUser = getUserInner(id);
    User otherUser = getUserInner(otherId);
    Set<Integer> mainUserFriends = Set.copyOf(mainUser.getFriends());
    mainUserFriends.retainAll(otherUser.getFriends());
    return mainUserFriends.stream()
            .map(userId -> storage.getUser(userId))
            .collect(Collectors.toList());
  }
  private User getUserInner(int id) {
    User user = storage.getUser(id);
    if (user == null) {
      throw new UserNotFoundException(id);
    }
    return user;
  }

}
