package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.filmorate.exceptions.db.CreateUserFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserRelation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String GET_LIST_OF_USERS_QUERY =
            "SELECT\n" +
            "    USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY\n" +
            "FROM \n" +
            "     USERS WHERE USER_ID in (:user_ids);";
    private static final String GET_USER_BY_ID_QUERY =
            "SELECT\n" +
            "    USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY\n" +
            "FROM\n" +
            "     USERS\n" +
            "WHERE\n" +
            "    USER_ID = :user_id;";
    private static final String GET_ALL_USERS_QUERY =
            "SELECT\n" +
            "    USER_ID, EMAIL, LOGIN, NAME, BIRTHDAY\n" +
            "FROM \n" +
            "     USERS;";
    private static final String UPDATE_USER_QUERY =
            "UPDATE USERS\n" +
            "    SET \n" +
            "        email = ?,\n" +
            "        login = ?,\n" +
            "        name = ?,\n" +
            "        birthday = ?\n" +
            "    WHERE \n" +
            "        USER_ID = ?;";

    @Override
    public List<User> getCommonFriends(User mainUser, User otherUser) {
        String sqlQuery = "SELECT\n" +
                "    users.USER_ID, users.email, users.login, users.name, users.birthday\n" +
                "FROM \n" +
                "     users\n" +
                "        left join USER_RELATIONS AS USER_RELATIONS_APPROVER ON USERS.USER_ID = USER_RELATIONS_APPROVER.REQUESTER_ID\n" +
                "        left join USER_RELATIONS AS USER_RELATIONS_REQUESTER ON USERS.USER_ID = USER_RELATIONS_REQUESTER.APPROVER_ID \n" +
                "WHERE\n" +
                "    (USER_RELATIONS_APPROVER.APPROVER_ID = :first_user_id AND USER_RELATIONS_APPROVER.ACCEPTED\n" +
                "    OR USER_RELATIONS_REQUESTER.REQUESTER_ID = :first_user_id)\n" +
                "INTERSECT \n" +
                "SELECT\n" +
                "    users.USER_ID, users.email, users.login, users.name, users.birthday\n" +
                "FROM \n" +
                "     users\n" +
                "        left join USER_RELATIONS AS USER_RELATIONS_APPROVER ON USERS.USER_ID = USER_RELATIONS_APPROVER.REQUESTER_ID\n" +
                "        left join USER_RELATIONS AS USER_RELATIONS_REQUESTER ON USERS.USER_ID = USER_RELATIONS_REQUESTER.APPROVER_ID \n" +
                "WHERE\n" +
                "    (USER_RELATIONS_APPROVER.APPROVER_ID = :second_user_id AND USER_RELATIONS_APPROVER.ACCEPTED\n" +
                "    OR USER_RELATIONS_REQUESTER.REQUESTER_ID = :second_user_id)";
        return new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(
                    sqlQuery,
                    Map.of("first_user_id", mainUser.getId(), "second_user_id", otherUser.getId()),
                    this::createNewUser);
    }

    @Override
    public void removeUserRelations(User firstUser, User secondUser) {
        new NamedParameterJdbcTemplate(jdbcTemplate).update(
                "DELETE FROM USER_RELATIONS WHERE REQUESTER_ID = :first_user AND APPROVER_ID = :second_user\n" +
                        "OR APPROVER_ID = :first_user AND REQUESTER_ID = :second_user",
                Map.of("first_user", firstUser.getId(), "second_user", secondUser.getId()));
    }

    @Override
    public void updateUserRelations(User requester, User approver, boolean accepted) {
        jdbcTemplate.update(
                "INSERT INTO USER_RELATIONS(REQUESTER_ID, APPROVER_ID, ACCEPTED) VALUES (?,?,?);",
                requester.getId(), approver.getId(), accepted);
    }

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
        jdbcTemplate.update(
                "INSERT INTO USERS(email, login, name, birthday) VALUES(?,?,?,?);",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday().format(formatter));
        Integer lastIndex = jdbcTemplate
                .queryForObject(
                        "Select user_id FROM Users order by user_id desc limit 1", Integer.class);
        if (lastIndex != null) {
            user.setId(lastIndex);
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        SqlRowSet set = jdbcTemplate.queryForRowSet(
                "SELECT user_id FROM USERS WHERE USER_ID = ?",
                user.getId());
        if (!set.next()) {
            throw new UserNotFoundException("Неизвестный идентификатор пользователя");
        }
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
        jdbcTemplate.update(
                UPDATE_USER_QUERY,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday().format(formatter), user.getId());
        return getUser(user.getId());
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query(GET_ALL_USERS_QUERY, this::createNewUser);
    }

    @Override
    public List<User> getUsers(List<Integer> ids) {
        return new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(GET_LIST_OF_USERS_QUERY, Map.of("user_ids", ids), this::createNewUser);
    }

    @Override
    public User getUser(int id) {
        return new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(GET_USER_BY_ID_QUERY, Map.of("user_id", id), this::createNewUser)
                .stream()
                .findAny()
                .orElse(null);
    }

    private User createNewUser(ResultSet resultSet, int rowNum) {
        User user = new User();

        try {
            user.setId(resultSet.getInt("user_id"));
            user.setEmail(resultSet.getString("email"));
            user.setLogin(resultSet.getString("login"));
            user.setName(resultSet.getString("name"));
            user.setBirthday(resultSet.getDate("birthday").toLocalDate());

        } catch (SQLException e) {
            throw new CreateUserFromDatabaseResultSetException(e);
        }
        user.setRelations(getUserRelations(user.getId()));
        return user;
    }

    private Set<UserRelation> getUserRelations(int id) {
        String sqlQuery = "SELECT REQUESTER_ID, APPROVER_ID, ACCEPTED\n" +
                "FROM USER_RELATIONS WHERE REQUESTER_ID = :user_id or APPROVER_ID = :user_id";

        return new HashSet<>(new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(sqlQuery, Map.of("user_id", id), (rs, rowNum) -> {
                    UserRelation relation = new UserRelation();
                    relation.setAccepted(rs.getBoolean("accepted"));
                    relation.setRequesterId(rs.getInt("requester_id"));
                    relation.setApproverId(rs.getInt("approver_id"));
                    return relation;
                }));
    }

    public boolean deleteUser(@PathVariable int userId) {
        String sqlQuery = "DELETE FROM users WHERE user_id=?;";
        return jdbcTemplate.update(sqlQuery, userId) > 0;
    }
}
