package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.db.CreateFilmFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getSortedFilms(int id, String sortBy) {
        StringBuilder sqlQuery = new StringBuilder(
                "SELECT\n" +
                        "    movies.movie_id as id,\n" +
                        "    movies.title AS movie_title,\n" +
                        "    movies.description AS movie_description,\n" +
                        "    release_date,\n" +
                        "    duration,\n" +
                        "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                        "    MPA_rating.title AS rating_title,\n" +
                        "    MPA_rating.DESCRIPTION AS rating_description\n" +
                        "FROM\n" +
                        "    movies INNER JOIN movies_directors ON movies_directors.movie_id = movies.movie_id AND movies_directors.director_id = ?\n" +
                        "    LEFT JOIN MPA_rating " +
                        "        ON movies.rating = MPA_rating.rating_id\n" +
                        "    LEFT JOIN MOVIES_LIKES " +
                        "        ON movies_likes.movie_id = movies.movie_id\n");

        if (sortBy.equals("year")) {
            sqlQuery.append("ORDER BY");
            sqlQuery.append(" EXTRACT(YEAR FROM movies.release_date) ");
            sqlQuery.append("ASC");
        } else if (sortBy.equals("likes")) {
            sqlQuery.append("GROUP BY id, movie_title, movie_description, release_date, duration, rating_id, rating_title, rating_description\n");
            sqlQuery.append("ORDER BY count(MOVIES_LIKES.*) ");
            sqlQuery.append("DESC");
        }

        List<Film> films = jdbcTemplate.query(sqlQuery.toString(), (rs, rowNum) -> (createNewFilm(rs)), id);

        fillInGenres(films);
        fillInLikes(films);
        fillInDirectors(films);
        return films;
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
        updateFilmDirectors(film, film.getId());
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
        fillInDirectors(films);
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
        fillInDirectors(films);
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

    private void updateFilmDirectors(Film film, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_DIRECTORS WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_DIRECTORS VALUES (?, ?)";
        film.getDirectors()
                .forEach(director -> {
                    jdbcTemplate.update(sqlQuery, filmId, director.getId());
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
        fillInDirectors(films);
        return films;
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

    @Override
    public boolean deleteFilm(int filmId) {
        String sqlQuery = "DELETE FROM movies WHERE movie_id=?;";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
    }

    private void fillInDirectors(List<Film> films) {
        List<Integer> filmsIds = films.stream()
                .mapToInt(Film::getId).boxed()
                .collect(Collectors.toList());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("ids", filmsIds);
        String sqlQuery = "SELECT movies_directors.movie_id, directors.director_id, directors.name\n" +
                " FROM movies_directors" +
                "     INNER JOIN directors ON directors.director_id = movies_directors.director_id\n" +
                " WHERE movies_directors.movie_id in (:ids)";
        SqlRowSet rowSet = new NamedParameterJdbcTemplate(jdbcTemplate).queryForRowSet(sqlQuery, parameters);

        while (rowSet.next()) {
            int movieId = rowSet.getInt("movie_id");
            films.stream()
                    .filter(film -> (film.getId() == movieId))
                    .findFirst()
                    .ifPresent(film -> {
                        Director director = new Director();
                        director.setId(rowSet.getInt("director_id"));
                        director.setName(rowSet.getString("name"));
                        film.getDirectors().add(director);
                    });
        }
    }
}
