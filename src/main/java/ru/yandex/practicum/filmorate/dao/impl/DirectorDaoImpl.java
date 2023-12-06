package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exceptions.db.CreateDirectorFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Repository
public class DirectorDaoImpl implements DirectorDao {

    private final JdbcTemplate jdbcTemplate;
    private static final String GET_DIRECTORS_QUERY = "SELECT director_id, name FROM directors";
    private static final String GET_DIRECTOR_BY_ID_QUERY =
            "SELECT director_id, name FROM directors where director_id = ?";
    private static final String ADD_NEW_DIRECTOR_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE director_id = ?";

    public DirectorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        return jdbcTemplate.query(GET_DIRECTORS_QUERY, this::mapRowToDirector);
    }

    @Override
    public Director getDirectorById(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_DIRECTOR_BY_ID_QUERY, this::mapRowToDirector, id);
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException(id);
        }
    }

    @Override
    public Director postDirector(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(ADD_NEW_DIRECTOR_QUERY, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director putDirector(Director director) {
        getDirectorById(director.getId());
        jdbcTemplate.update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        getDirectorById(id);
        jdbcTemplate.update(DELETE_DIRECTOR_QUERY, id);
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) {
        Director director = new Director();

        try {
            director.setName(resultSet.getString("name"));
            director.setId(resultSet.getInt("director_id"));
        } catch (SQLException e) {
            throw new CreateDirectorFromDatabaseResultSetException(e);
        }
        return director;
    }
}
