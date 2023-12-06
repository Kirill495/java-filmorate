package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.db.CreateFilmFromDatabaseResultSetException;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
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
        updateFilmDirectors(film, filmId);
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
        updateFilmGenres(film, film.getId());
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
    public List<Film> getRecommendations(int userId) {
        String sqlQuery =
                "--Получаем итоговый список фильмов из той же movies_likes через лайки:-------------------------\n" +
                        "SELECT DISTINCT\n" +
                        "    movies.movie_id as id,\n" +
                        "    movies.title AS movie_title,\n" +
                        "    movies.description AS movie_description,\n" +
                        "    movies.release_date,\n" +
                        "    movies.duration,\n" +
                        "    CASE WHEN movies.rating IS NULL THEN 0 ELSE movies.rating END AS rating_id,\n" +
                        "    MPA_rating.title AS rating_title,\n" +
                        "    MPA_rating.DESCRIPTION AS rating_description\n" +
                        "FROM movies_likes ul\n" +
                        "LEFT JOIN movies_likes ul2 ON ul.movie_id = ul2.movie_id AND ul2.user_id = :user_id\n" +
                        "INNER JOIN movies ON ul.movie_id = movies.movie_id\n" +
                        "LEFT JOIN MPA_rating ON movies.rating = MPA_rating.rating_id\n" +
                        "---------------------------------------------------------------------------------------------\n" +
                        "WHERE ul2.user_id IS NULL AND ul.user_id IN \n" +
                        "	--Найдем пользователя с которым больше всего лайков--------------------------------------\n" +
                        "	(SELECT \n" +
                        "		ul2.user_id\n" +
                        "	FROM movies_likes ul1\n" +
                        "	INNER JOIN movies_likes ul2\n" +
                        "		ON ul1.movie_id = ul2.movie_id\n" +
                        "			AND ul1.user_id != ul2.user_id --не учитываем этот же фильм\n" +
                        "	WHERE ul1.user_id = :user_id\n" +
                        "	GROUP BY  ul2.user_id\n" +
                        "	HAVING ul2.user_id IN\n" +
                        "				--Найти всех пользователей имеющих лайки, которых нет у данного пользователя:\n" +
                        "				(SELECT DISTINCT ul.USER_ID\n" +
                        "				FROM movies_likes AS ul			\n" +
                        "				LEFT JOIN movies_likes ul2 ON ul.movie_id = ul2.movie_id AND ul2.user_id = :user_id\n" +
                        "				WHERE ul2.user_id IS NULL\n" +
                        "				)\n" +
                        "	ORDER BY COUNT(*) DESC LIMIT 1 --Сортируем по количеству лайков и отбираем первый сверху \n" +
                        "	)"
                ;
        List<Film> films = new NamedParameterJdbcTemplate(jdbcTemplate).query(sqlQuery, Map.of("user_id", userId), (rs, rowNum) -> createNewFilm(rs));
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
        fillInDirectors(films);
        return films.get(0);
    }

    public List<Film> getFilms(List<Integer> ids) {
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
                        "    movies.movie_id in (:movie_ids)";
        List<Film> films = new NamedParameterJdbcTemplate(jdbcTemplate)
                .query(sqlQuery, Map.of("movie_ids", ids), (rs, rowNum) -> createNewFilm(rs));
        if (films.size() == 0) {
            return null;
        }
        fillInGenres(films);
        fillInLikes(films);
        fillInDirectors(films);
        return films;
    }

    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = getFilmsWithRating(count);
        if (films.size() < count) {
            films.addAll(getFilmsWithoutRating(count - films.size()));
        }
        fillInGenres(films);
        fillInLikes(films);
        fillInDirectors(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sqlQuery = "SELECT\n" +
                "user_likes.MOVIE_ID AS id,\n" +
                "MOVIES.TITLE AS movie_title,\n" +
                "MOVIES.DESCRIPTION AS movie_description,\n" +
                "MOVIES.RELEASE_DATE AS release_date,\n" +
                "MOVIES.DURATION AS duration,\n" +
                "MPA_RATING.rating_id as rating_id,\n" +
                "MPA_RATING.description as rating_description,\n" +
                "MPA_RATING.title as rating_title\n" +
                "FROM\n" +
                "MOVIES_LIKES as user_likes\n" +
                "INNER JOIN MOVIES_LIKES AS friend_likes\n" +
                "ON user_likes.MOVIE_ID = friend_likes.MOVIE_ID\n" +
                "AND friend_likes.USER_ID = ?\n" +
                "INNER JOIN MOVIES\n" +
                "INNER JOIN MPA_RATING\n" +
                "ON MOVIES.rating = MPA_RATING.rating_id\n" +
                "ON user_likes.MOVIE_ID = MOVIES.MOVIE_ID\n" +
                "WHERE\n" +
                "user_likes.USER_ID = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)), userId, friendId);
    }


    @Override
    public boolean deleteFilm(int filmId) {
        String sqlQuery = "DELETE FROM movies WHERE movie_id=?;";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
    }

    @Override
    public List<Film> getMostPopularFilmsFilterAll(Integer limit, Integer genreId, Integer year) {
        if (year == null && genreId == null) {
            return getMostPopularFilms(limit);
        } else if (year != null && genreId == null) {
            return getMostPopularFilmsFilterByYear(limit, year);
        } else if (year == null && genreId != null) {
            return getMostPopularFilmsFilterByGenre(limit, genreId);
        } else {
            return requestPopularFilmsFilterByYearAndGenre(limit, genreId, year);
        }
    }

    public List<Film> requestPopularFilmsFilterByYearAndGenre(int limit, int genreId, int year) {
        try {
            String sqlQuery = "SELECT m.movie_id AS id, m.title AS movie_title, m.description AS movie_description, " +
                    "EXTRACT(YEAR FROM m.release_date), m.duration, " +
                    "r.rating_id, r.title AS rating_title, r.description AS rating_description, COUNT(l.movie_id), m.release_date " +
                    "FROM movies AS m " +
                    "LEFT JOIN movies_likes AS l ON m.movie_id = l.movie_id " +
                    "INNER JOIN mpa_rating AS r ON m.rating = r.rating_id " +
                    "INNER JOIN movies_genres AS g ON m.movie_id=g.movie_id " +
                    "WHERE EXTRACT(YEAR FROM m.release_date)=? AND g.genre_id=? " +
                    "GROUP BY m.movie_id " +
                    "ORDER BY COUNT(l.movie_id) DESC, m.movie_id " +
                    "LIMIT ?;";

            List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)), year, genreId, limit);
            fillInGenres(films);
            fillInLikes(films);
            fillInDirectors(films);
            return films;
        } catch (DataAccessException e) {
            return new ArrayList<>();
        }
    }

    public List<Film> getMostPopularFilmsFilterByGenre(int limit, int genreId) {
        try {
            String sqlQuery = "SELECT m.movie_id AS id, m.title AS movie_title, m.description AS movie_description, m.duration, " +
                    "r.rating_id, r.title AS rating_title, r.description AS rating_description, COUNT(l.movie_id), m.release_date " +
                    "FROM movies AS m " +
                    "LEFT JOIN movies_likes AS l ON m.movie_id = l.movie_id " +
                    "INNER JOIN mpa_rating AS r ON m.rating = r.rating_id " +
                    "INNER JOIN movies_genres AS g ON m.movie_id=g.movie_id " +
                    "WHERE g.genre_id=? " +
                    "GROUP BY m.movie_id " +
                    "ORDER BY COUNT(l.movie_id) DESC, m.movie_id " +
                    "LIMIT ?;";
            List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)), genreId, limit);
            fillInGenres(films);
            fillInLikes(films);
            fillInDirectors(films);
            return films;
        } catch (DataAccessException e) {
            return new ArrayList<>();
        }
    }

    public List<Film> getMostPopularFilmsFilterByYear(int limit, int year) {

        String sqlQuery = "SELECT m.movie_id AS id, m.title AS movie_title, m.description AS movie_description, " +
                "EXTRACT(YEAR FROM m.release_date), m.duration, " +
                "r.rating_id, r.title AS rating_title, r.description AS rating_description, COUNT(l.movie_id), m.release_date " +
                "FROM movies AS m " +
                "LEFT JOIN movies_likes AS l ON m.movie_id = l.movie_id " +
                "INNER JOIN mpa_rating AS r ON m.rating = r.rating_id " +
                "INNER JOIN movies_genres AS g ON m.movie_id=g.movie_id " +
                "WHERE EXTRACT(YEAR FROM m.release_date)=? " +
                "GROUP BY m.movie_id " +
                "ORDER BY COUNT(l.movie_id) DESC, m.movie_id " +
                "LIMIT ?;";
        try {
            List<Film> films = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> (createNewFilm(rs)), year, limit);
            fillInGenres(films);
            fillInLikes(films);
            fillInDirectors(films);
            return films;
        } catch (DataAccessException e) {
            return new ArrayList<>();
        }
    }

    private void updateFilmGenres(Film film, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_GENRES WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_GENRES VALUES (?, ?)";
        film.getGenres()
                .forEach(genre -> jdbcTemplate.update(sqlQuery, filmId, genre.getId()));
    }

    private void updateLikes(Set<Integer> likes, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_LIKES WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_LIKES VALUES (?, ?)";
        likes.forEach(userId -> jdbcTemplate.update(sqlQuery, filmId, userId));
    }

    private void updateFilmDirectors(Film film, int filmId) {
        jdbcTemplate.update("DELETE FROM MOVIES_DIRECTORS WHERE movie_id = ?", filmId);
        String sqlQuery = "INSERT INTO MOVIES_DIRECTORS VALUES (?, ?)";
        film.getDirectors()
                .forEach(director -> jdbcTemplate.update(sqlQuery, filmId, director.getId()));
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
    public List<Film> getFilmsBySearchParameters(String query, Set<String> queryParameters) {
        StringBuilder sqlQuery = new StringBuilder();
        if (queryParameters.contains("TITLE")) {
            sqlQuery.append(
                    "SELECT\n" +
                    "    MOVIE_ID\n" +
                    "FROM\n" +
                    "    MOVIES\n" +
                    "WHERE\n" +
                    "    UPPER(MOVIES.TITLE) LIKE :query");
        }
        if (queryParameters.contains("DIRECTOR")) {
            if (sqlQuery.length() != 0) {
                sqlQuery.append("\n" +
                    "UNION\n" +
                    "\n");
            }
            sqlQuery.append(
                    "SELECT\n" +
                    "    MOVIE_ID\n" +
                    "FROM\n" +
                    "    DIRECTORS join movies_directors ON DIRECTORS.director_id = movies_directors.director_id\n" +
                    "WHERE\n" +
                    "    UPPER(DIRECTORS.NAME) LIKE :query");
        }
        List<Integer> filmsIds;
        try {
            filmsIds = new NamedParameterJdbcTemplate(jdbcTemplate)
                    .queryForList(
                            sqlQuery.toString(),
                            Map.of("query", "%" + query.toUpperCase() + "%"),
                            Integer.class);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        if (filmsIds.isEmpty()) {
            return new ArrayList<>();
        }
        return getFilms(filmsIds)
                .stream()
                .sorted(Comparator.comparingInt(film -> -1 * film.getDirectors().size()))
                .collect(Collectors.toList());
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
                    .ifPresent(film -> film.getLikes().add(rowSet.getInt("user_id")));
        }
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
