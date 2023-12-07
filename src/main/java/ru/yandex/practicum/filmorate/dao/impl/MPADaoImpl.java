package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MPADao;
import ru.yandex.practicum.filmorate.exceptions.db.CreateMPAFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.mpa.MPANotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MPADaoImpl implements MPADao {
    private final JdbcTemplate jdbcTemplate;
    private static final String GET_MPA_BY_ID_QUERY =
            "SELECT rating_id, title, description FROM MPA_RATING WHERE rating_id = ?";
    private static final String GET_ALL_MPA_QUERY = "SELECT rating_id, title, description FROM MPA_RATING";

    @Autowired
    public MPADaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MPA findMPARatingById(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_MPA_BY_ID_QUERY, this::mapRowToRating, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MPANotFoundException(id);
        }
    }

    @Override
    public List<MPA> findAllMPARating() {
        return jdbcTemplate.query(GET_ALL_MPA_QUERY, this::mapRowToRating);
    }

    private MPA mapRowToRating(ResultSet resultSet, int rowNum) {
        MPA rating = new MPA();
        try {
            rating.setId(resultSet.getInt("rating_id"));
            rating.setName(resultSet.getString("title"));
            rating.setDescription(resultSet.getString("description"));
        } catch (SQLException e) {
            throw new CreateMPAFromDatabaseResultSetException(e);
        }
        return rating;
    }
}
