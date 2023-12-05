package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    void updateUserRelations(User requester, User approver, boolean accepted);

    void removeUserRelations(User firstUser, User secondUser);

    List<User> getUsers();

    User getUser(int id);

    List<User> getCommonFriends(User mainUser, User otherUser);

    boolean deleteUser(int userId);
}
