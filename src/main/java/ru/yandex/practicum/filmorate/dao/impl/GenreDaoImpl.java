package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exceptions.genre.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.db.CreateGenreFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class GenreDaoImpl implements GenreDao {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre findGenreById(int id) {
        String sqlQuery = "SELECT genre_id, title FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException(id);
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        String sqlQuery = "SELECT genre_id, title FROM genres";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) {
        Genre genre = new Genre();

        try {
            genre.setName(resultSet.getString("title"));
            genre.setId(resultSet.getInt("genre_id"));
        } catch (SQLException e) {
            throw new CreateGenreFromDatabaseResultSetException(e);
        }
        return genre;
    }

}
