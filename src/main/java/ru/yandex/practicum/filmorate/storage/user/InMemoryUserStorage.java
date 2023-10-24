package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.user.UserDataValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {

  private final Map<Integer, User> users = new HashMap<>();

  @Override
  public User addUser(User user) {
    int maxId = users.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    user.setId(maxId + 1);
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public User updateUser(User user) {
    if (!users.containsKey(user.getId())) {
      throw new UserDataValidationException("Неизвестный идентификатор пользователя");
    }
    users.put(user.getId(), user);
    return user;
  }

  @Override
  public List<User> getUsers() {
    return new ArrayList<>(users.values());
  }

  @Override
  public User getUser(int id) {
    return users.get(id);
  }
}
