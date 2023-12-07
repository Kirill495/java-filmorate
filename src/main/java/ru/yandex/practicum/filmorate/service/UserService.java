package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.user.UserDataValidationException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage storage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage storage) {
        this.storage = storage;
    }

    public User addUser(User user) {
        fillInUserName(user);
        return storage.addUser(user);
    }

    public User updateUser(User user) {
        fillInUserName(user);
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
        User user = getUserInner(userId);
        User friend = getUserInner(friendId);
        Set<UserRelation> relations = user.getRelations();
        Predicate<UserRelation> counterRequestIsAlreadySent = r -> ((r.getApproverId() == userId && r.getRequesterId() == friendId));
        Predicate<UserRelation> requestIsAlreadySent = r -> ((r.getRequesterId() == userId && r.getApproverId() == friendId));
        if (relations.stream().anyMatch(counterRequestIsAlreadySent)) {
            // запрос уже был отправлен с противоположной стороны
            storage.updateUserRelations(user, friend, true);
        }
        if (relations.stream().noneMatch(requestIsAlreadySent)) {
            storage.updateUserRelations(user, friend, false);
        }
    }

    public void deleteFriend(int userId, int friendId) {
        User user = getUserInner(userId);
        User friend = getUserInner(friendId);
        storage.removeUserRelations(user, friend);
    }

    public List<User> getFriends(int id) {
        User user = getUserInner(id);
        return user.getRelations().stream().filter(rel -> (rel.isAccepted() || rel.getApproverId() == id))
                .map(r -> ((r.getApproverId() == id ? r.getRequesterId() : r.getApproverId())))
                .map(storage::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User mainUser = getUserInner(id);
        User otherUser = getUserInner(otherId);

        return storage.getCommonFriends(mainUser, otherUser);
    }

    private User getUserInner(int id) {
        User user = storage.getUser(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    private void fillInUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
