package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
@Qualifier("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private static final String UNSUPPORTED_OPERATION =
            "Этот метод невозможно вызвать через InMemoryUserStorage, попробуйте через UserDbStorage";

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
            throw new UserNotFoundException("Неизвестный идентификатор пользователя");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void updateUserRelations(User requester, User approver, boolean accepted) {

    }

    @Override
    public void removeUserRelations(User firstUser, User secondUser) {

        Predicate<UserRelation> relationExists = rel -> (
                rel.getRequesterId() == firstUser.getId() && rel.getApproverId() == secondUser.getId()
                        || rel.getRequesterId() == secondUser.getId() && rel.getApproverId() == firstUser.getId());
        firstUser.getRelations().removeIf(relationExists);
        secondUser.getRelations().removeIf(relationExists);

    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<User> getUsers(List<Integer> ids) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public User getUser(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getCommonFriends(User mainUser, User otherUser) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean deleteUser(@PathVariable int userId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }
}
