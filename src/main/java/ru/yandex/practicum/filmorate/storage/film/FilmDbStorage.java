package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.db.CreateFilmFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        int filmId = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("movies")
                .usingGeneratedKeyColumns("movie_id")
                .executeAndReturnKey(film.toMap()).intValue();
        updateFilmGenres(film, filmId);
        updateLikes(film.getLikes(), filmId);
        return getFilm(filmId);
    }

    @Override
    public Film updateFilm(Film film) {

        if (getFilm(film.getId()) == null) {
            throw new FilmNotFoundException(film.getId());
        }
        String sqlQuery = "UPDATE MOVIES\n" +
                "    set title = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, rating = ?\n" +
                "WHERE\n" +
                "    MOVIE_ID = ?";
        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        updateFilmGenres(film);
        updateLikes(film.getLikes(), film.getId());
        return getFilm(film.getId());
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery =
                "SELECT\n" +
                        "    movie_id as id,\n" +
                        "    movies.title AS movie_title,\n" +
                        "    movies.description AS movie_description,\n" +
                        "    release_date,\n" +
                        "    duration,\n" +
                        "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                        "    MPA_rating.title AS rating_title,\n" +
                        "    MPA_rating.DESCRIPTION AS rating_description\n" +
                        "FROM\n" +
                        "    movies " +
                        "    LEFT JOIN MPA_rating " +
                        "        ON movies.rating = MPA_rating.rating_id";
        List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)));
        fillInGenres(films);
        fillInLikes(films);
        return films;
    }

    @Override
    public Film getFilm(int id) {
        String sqlQuery =
                "SELECT\n" +
                        "    movie_id as id,\n" +
                        "    movies.title AS movie_title,\n" +
                        "    movies.description AS movie_description,\n" +
                        "    release_date,\n" +
                        "    duration,\n" +
                        "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                        "    MPA_rating.title AS rating_title,\n" +
                        "    MPA_rating.DESCRIPTION AS rating_description\n" +
                        "FROM\n" +
                        "    movies " +
                        "    LEFT JOIN MPA_rating " +
                        "        ON movies.rating = MPA_rating.rating_id\n" +
                        "WHERE\n" +
                        "    movies.movie_id = :movie_id\n" +
                        "LIMIT 1";
        List<Film> films = new NamedParameterJdbcTemplate(jdbcTemplate).query(sqlQuery, Map.of("movie_id", id), (rs, rowNum) -> createNewFilm(rs));
        if (films.size() == 0) {
            return null;
        }
        fillInGenres(films);
        fillInLikes(films);
        return films.get(0);
    }

    private void updateFilmGenres(Film film) {
        updateFilmGenres(film, film.getId());
    }

    private void updateFilmGenres(Film film, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_GENRES WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_GENRES VALUES (?, ?)";
        film.getGenres()
                .forEach(genre -> {
                    jdbcTemplate.update(sqlQuery, filmId, genre.getId());
                });
    }

    private void updateLikes(Set<Integer> likes, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_LIKES WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_LIKES VALUES (?, ?)";
        likes.forEach(userId -> {
            jdbcTemplate.update(sqlQuery, filmId, userId);
        });
    }

    private Film createNewFilm(ResultSet resultSet) {
        MPA ratingItem = null;
        try {
            if (resultSet.getInt("rating_id") != 0) {
                ratingItem = new MPA();
                ratingItem.setId(resultSet.getInt("rating_id"));
                ratingItem.setName(resultSet.getString("rating_title"));
                ratingItem.setDescription(resultSet.getString("rating_description"));
            }
        } catch (SQLException e) {
            throw new CreateFilmFromDatabaseResultSetException(e);
        }
        Film film = new Film();
        try {
            film.setId(resultSet.getInt("id"));
            film.setName(resultSet.getNString("movie_title"));
            film.setDescription(resultSet.getNString("movie_description"));
            film.setDuration(resultSet.getInt("duration"));
            film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        } catch (SQLException e) {
            throw new CreateFilmFromDatabaseResultSetException(e);
        }
        film.setMpa(ratingItem);
        return film;
    }

    @Override
    public List<Film> getTheMostPopularFilms(int count) {
        List<Film> films = getFilmsWithRating(count);
        if (films.size() < count) {
            films.addAll(getFilmsWithoutRating(count - films.size()));
        }
        fillInGenres(films);
        fillInLikes(films);
        return films;
    }

    @Override //Testing new strings in SQL query
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT" +
                " m.movie_id as movie_id," +
                " m.title as movie_name," +
                " m.description as movie_description," +
                " m.release_date as movie_release_date," +
                " m.duration as movie_duration," +
                " m.rating as mpa_id," +
                " mpa.description as mpa_name" +
                " FROM MOVIES m JOIN MPA_RATING mpa ON m.rating=mpa.rating_id" +
                " WHERE MOVIE_ID IN ALL (SELECT MOVIE_ID FROM MOVIES_LIKES" +
                " WHERE USER_ID = :userId AND MOVIE_ID IN ALL (SELECT MOVIE_ID FROM MOVIES_LIKES" +
                " WHERE USER_ID = :friendId ALL ORDER BY MOVIE_ID DESC))";
        List<Film> films = jdbcTemplate.query(sqlQuery,
                new MapSqlParameterSource[]{new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("friendId", friendId)}, this::mapToFilm);
        return films;
    }

    private Film mapToFilm(ResultSet rs, int i) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("movie_id"));
        film.setName(rs.getString("movie_name"));
        film.setDescription(rs.getString("movie_description"));
        film.setReleaseDate(rs.getObject("movie_release_date", LocalDate.class));
        film.setDuration(rs.getInt("movie_duration"));
        MPA mpa = new MPA();
        mpa.setName(rs.getString("mpa_name"));
        mpa.setId(rs.getInt("mpa_id"));
        film.setMpa(mpa);
        return film;
    }

    private List<Film> getFilmsWithRating(int count) {
        String sqlQuery = String.format("SELECT\n" +
                "    movies.movie_id as id,\n" +
                "    movies.title AS movie_title,\n" +
                "    movies.description AS movie_description,\n" +
                "    release_date,\n" +
                "    duration,\n" +
                "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                "    MPA_rating.title AS rating_title,\n" +
                "    MPA_rating.DESCRIPTION AS rating_description\n" +
                "FROM\n" +
                "    movies\n" +
                "    LEFT JOIN MPA_rating\n" +
                "    ON movies.rating = MPA_rating.rating_id\n" +
                "    inner JOIN        \n" +
                "            (SELECT\n" +
                "                MOVIE_ID, COUNT(USER_ID) as likes\n" +
                "            FROM\n" +
                "                MOVIES_LIKES\n" +
                "            GROUP BY\n" +
                "                MOVIE_ID\n" +
                "            LIMIT %d) AS top_movies\n" +
                "    ON movies.MOVIE_ID = top_movies.MOVIE_ID order by top_movies.likes desc", count);
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)));
    }

    private List<Film> getFilmsWithoutRating(int count) {

        String sqlQuery = String.format("SELECT\n" +
                "    movies.movie_id as id,\n" +
                "    movies.title AS movie_title,\n" +
                "    movies.description AS movie_description,\n" +
                "    release_date,\n" +
                "    duration,\n" +
                "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                "    MPA_rating.title AS rating_title,\n" +
                "    MPA_rating.DESCRIPTION AS rating_description\n" +
                "FROM\n" +
                "    movies\n" +
                "    LEFT JOIN MPA_rating\n" +
                "    ON movies.rating = MPA_rating.rating_id\n" +
                "    LEFT JOIN  MOVIES_LIKES ON movies.movie_id = MOVIES_LIKES.movie_id \n" +
                "    WHERE MOVIES_LIKES.movie_id IS NULL LIMIT %d", count);
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)));
    }

    private void fillInGenres(List<Film> films) {
        List<Integer> filmsIds = films.stream()
                .mapToInt(Film::getId).boxed()
                .collect(Collectors.toList());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", filmsIds);
        String sqlQuery = "SELECT movies.movie_id, genres.genre_id, genres.title as genre_title FROM movies inner join movies_genres\n" +
                "inner join genres on movies_genres.genre_id = genres.genre_id on movies.movie_id = movies_genres.movie_id\n" +
                "WHERE movies.movie_id in (:ids);";
        SqlRowSet rowSet = new NamedParameterJdbcTemplate(jdbcTemplate).queryForRowSet(sqlQuery, parameters);

        while (rowSet.next()) {
            int movieId = rowSet.getInt("movie_id");
            films.stream()
                    .filter(film -> (film.getId() == movieId))
                    .findFirst()
                    .ifPresent(film -> {
                        Genre genre = new Genre();
                        genre.setId(rowSet.getInt("genre_id"));
                        genre.setName(rowSet.getString("title"));
                        film.getGenres().add(genre);
                    });
        }
    }

    private void fillInLikes(List<Film> films) {
        List<Integer> filmsIds = films.stream()
                .mapToInt(Film::getId).boxed()
                .collect(Collectors.toList());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", filmsIds);
        String sqlQuery = "SELECT MOVIE_ID, USER_ID\n" +
                "FROM MOVIES_LIKES WHERE MOVIE_ID in (:ids)";
        SqlRowSet rowSet = new NamedParameterJdbcTemplate(jdbcTemplate).queryForRowSet(sqlQuery, parameters);

        while (rowSet.next()) {
            int movieId = rowSet.getInt("movie_id");
            films.stream()
                    .filter(film -> (film.getId() == movieId))
                    .findFirst()
                    .ifPresent(film -> {
                        film.getLikes().add(rowSet.getInt("user_id"));
                    });
        }
    }
}
