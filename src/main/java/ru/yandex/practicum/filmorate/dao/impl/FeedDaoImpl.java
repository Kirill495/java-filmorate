package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FeedDao;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
public class FeedDaoImpl implements FeedDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String INSERT_NEW_FEED_QUERY =
            "INSERT INTO feed (user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?)";
    private static final String GET_USER_FEEDS_QUERY =
            "SELECT f.event_id, f.event_time, f.user_id, f.event_type, f.operation, f.entity_id " +
            "FROM feed AS f " +
            "WHERE f.user_id = ? " +
            "ORDER BY f.event_time";

    @Autowired
    public FeedDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Feed postEvent(Feed feed) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(INSERT_NEW_FEED_QUERY, new String[]{"event_id", "event_time"});
            stmt.setInt(1, feed.getUserId());
            stmt.setString(2, feed.getEventType());
            stmt.setString(3, feed.getOperation());
            stmt.setInt(4, feed.getEntityId());
            return stmt;
        }, keyHolder);
        feed.setEventId(Integer.parseInt(Objects.requireNonNull(keyHolder.getKeys()).get("event_id").toString()));
        feed.setTimestamp(((Timestamp) Objects.requireNonNull(keyHolder.getKeys()).get("event_time")).getTime());
        return feed;
    }

    @Override
    public List<Feed> getEvents(int userId) {
        return jdbcTemplate.query(
                GET_USER_FEEDS_QUERY,
                (rs, rowNum) -> (Feed.builder()
                        .withEventId(rs.getInt("event_id"))
                        .withTimestamp(rs.getTimestamp("event_time").getTime())
                        .withUserId(rs.getInt("user_id"))
                        .withEventType(rs.getString("event_type"))
                        .withEntityId(rs.getInt("entity_id"))
                        .withOperation(rs.getString("operation"))
                        .build()),
                userId);
    }
}
