package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class EventDaoImpl implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Event postEvent(Event event) {
        String sqlInsert = "INSERT INTO events (event_time, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlInsert, new String[]{"director_id"});
            if (event.getEventTime() != null)
                stmt.setTimestamp(1, Timestamp.valueOf(event.getEventTime().toLocalDateTime()));
            stmt.setInt(2, event.getUserId());
            stmt.setString(4, event.getEventType());
            stmt.setString(5, event.getOperation());
            stmt.setInt(5, event.getEntityId());
            return stmt;
        }, keyHolder);
        event.setEventId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return event;
    }

    @Override
    public List<Event> getEvents(int userId) {
        String sql = "SELECT event_id, event_time, user_id, event_type, operation, entity_id " +
                "FROM events " +
                "WHERE user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeEvent(rs), userId);
    }

    private Event makeEvent(ResultSet rs) throws SQLException {
        int eventId = rs.getInt("event_id");
        ZonedDateTime eventTime = rs.getTimestamp("event_time").toLocalDateTime().atZone(ZoneId.systemDefault());
        int userId = rs.getInt("user_id");
        String eventType = rs.getString("event_type");
        String operation = rs.getString("operation");
        int entityId = rs.getInt("entity_id");
        return new Event(eventId, eventTime, userId, eventType, operation, entityId);
    }
}
