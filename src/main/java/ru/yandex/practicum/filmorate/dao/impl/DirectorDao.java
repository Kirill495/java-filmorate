package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.db.CreateGenreFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.director.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class DirectorDao implements ru.yandex.practicum.filmorate.dao.DirectorDao {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        String sqlQuery = "SELECT director_id, name FROM directors";

        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Director getDirectorById(int id) {
        String sqlQuery = "SELECT director_id, name FROM directors where director_id = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToDirector, id);
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException(id);
        }
    }

    @Override
    public Director postDirector(Director director) {
        String sqlInsert = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlInsert, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);
        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director putDirector(Director director) {
        getDirectorById(director.getId());
        String sqlUpdate = "UPDATE directors SET name = ? WHERE director_id = ?";

        jdbcTemplate.update(sqlUpdate,
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        getDirectorById(id);
        String sqlUpdate = "delete from directors WHERE director_id = ?";

        jdbcTemplate.update(sqlUpdate, id);
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) {
        Director director = new Director();

        try {
            director.setName(resultSet.getString("name"));
            director.setId(resultSet.getInt("director_id"));
        } catch (SQLException e) {
            throw new CreateGenreFromDatabaseResultSetException(e);
        }
        return director;
    }
}
