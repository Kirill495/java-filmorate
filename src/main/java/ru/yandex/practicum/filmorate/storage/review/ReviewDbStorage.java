package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.db.CRUDReviewInDatabaseException;
import ru.yandex.practicum.filmorate.exceptions.db.CreateReviewFromDatabaseException;
import ru.yandex.practicum.filmorate.exceptions.review.ReviewMarksAddException;
import ru.yandex.practicum.filmorate.exceptions.review.ReviewMarksRemoveException;
import ru.yandex.practicum.filmorate.exceptions.review.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String UPDATE_REVIEW_QUERY = "UPDATE REVIEWS\n" +
                                                    "    SET CONTENT = ?, ISPOSITIVE = ?\n" +
                                                    "WHERE\n" +
                                                    "      REVIEW_ID = ?";
    private static final String REMOVE_REVIEW_QUERY = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
    private static final String ADD_MARK_TO_REVIEW_QUERY =
            "INSERT INTO REVIEWS_ESTIMATIONS(review_id, user_id, mark) VALUES (?, ?, ?);";
    private static final String REMOVE_MARK_OF_REVIEW =
            "DELETE FROM REVIEWS_ESTIMATIONS WHERE REVIEW_ID = ? and USER_ID = ? and mark = ?;";
    private static final String GET_REVIEW_BY_ID_QUERY =
            "SELECT \n" +
            "        REVIEWS.REVIEW_ID as id,\n" +
            "        REVIEWS.USER_ID as userId,\n" +
            "        REVIEWS.MOVIE_ID as movieId,\n" +
            "        MAX(REVIEWS.CONTENT) AS content,\n" +
            "        MAX(REVIEWS.ISPOSITIVE) AS isPositive,\n" +
            "        SUM(IFNULL(RE.MARK, 0)) AS useful\n" +
            "FROM REVIEWS\n" +
            "    LEFT JOIN REVIEWS_ESTIMATIONS RE ON REVIEWS.REVIEW_ID = RE.REVIEW_ID\n" +
            "WHERE \n" +
            "    REVIEWS.REVIEW_ID = ?\n" +
            "GROUP BY \n" +
            "    REVIEWS.REVIEW_ID, REVIEWS.USER_ID, REVIEWS.MOVIE_ID";
    private static final String GET_REVIEWS_QUERY_TEMPLATE =
            "SELECT\n" +
            "    R.REVIEW_ID as id, MAX(R.CONTENT) as content,  MAX(R.ISPOSITIVE) as isPositive, " +
            "    MAX(R.USER_ID) as userId, MAX(R.MOVIE_ID) as movieId,\n" +
            "    SUM(IFNULL(RE.MARK, 0)) AS useful\n" +
            "FROM\n" +
            "    REVIEWS R\n" +
            "    left join REVIEWS_ESTIMATIONS RE on R.REVIEW_ID = RE.REVIEW_ID\n" +
            "WHERE %s\n" +
            "GROUP BY\n" +
            "    R.REVIEW_ID\n" +
            "ORDER BY useful DESC, id \n" +
            "LIMIT %d;";

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
        int reviewId;
        try {
            reviewId = new SimpleJdbcInsert(jdbcTemplate)
                    .withTableName("REVIEWS")
                    .usingGeneratedKeyColumns("review_id")
                    .executeAndReturnKey(review.toMap()).intValue();
        } catch (DataAccessException e) {
            throw new CRUDReviewInDatabaseException("Не удалось добавить отзыв в базу данных", e);
        }
        return getReview(reviewId);
    }

    @Override
    public Review updateReview(Review review) {
        if (getReview(review.getReviewId()) == null) {
            throw new ReviewNotFoundException(review.getReviewId());
        }
        try {
            jdbcTemplate.update(UPDATE_REVIEW_QUERY,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId());
        } catch (DataAccessException e) {
            throw new CRUDReviewInDatabaseException("Не удалось обновить отзыв в базе данных.", e);
        }
        return getReview(review.getReviewId());
    }

    @Override
    public void removeReview(int id) {
        if (getReview(id) == null) {
            throw new ReviewNotFoundException(id);
        }
        try {
            jdbcTemplate.update(REMOVE_REVIEW_QUERY, id);
        } catch (DataAccessException e) {
            throw new CRUDReviewInDatabaseException("Не удалось удалить отзыв из базы данных", e);
        }
    }

    @Override
    public Review getReview(int id) {
        try {
            return jdbcTemplate.queryForObject(GET_REVIEW_BY_ID_QUERY, this::mapRowToReview, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException(id);
        }
    }

    @Override
    public List<Review> getReviews(int movieId, int count) {
        String sqlQuery = String.format(GET_REVIEWS_QUERY_TEMPLATE,
                (movieId == 0) ? "TRUE" : "R.MOVIE_ID = ?",
                count);
        try {
            if (movieId == 0) {
                return jdbcTemplate.query(sqlQuery, this::mapRowToReview);
            } else {
                return jdbcTemplate.query(sqlQuery, this::mapRowToReview, movieId);
            }
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void addMarkToReview(int reviewId, int userId, int mark) {
        try {
            jdbcTemplate.update(ADD_MARK_TO_REVIEW_QUERY, reviewId, userId, mark);
        } catch (DataAccessException e) {
            throw new ReviewMarksAddException("Ошибка при добавлении оценки отзыву", e);
        }
    }

    @Override
    public void removeMarkOfReview(int reviewId, int userId, int mark) {
        try {
            jdbcTemplate.update(REMOVE_MARK_OF_REVIEW, reviewId, userId, mark);
        } catch (DataAccessException e) {
            throw new ReviewMarksRemoveException("Ошибка при удалении оценки у отзыва", e);
        }
    }

    private Review mapRowToReview(ResultSet resultSet, int rowNum) {
        try {
            return Review.builder()
                    .withReviewId(resultSet.getInt("id"))
                    .withContent(resultSet.getString("content"))
                    .withFilmId(resultSet.getInt("movieId"))
                    .withUserId(resultSet.getInt("userId"))
                    .withUseful(resultSet.getInt("isPositive"))
                    .withIsPositive(resultSet.getBoolean("isPositive"))
                    .build();
        } catch (SQLException e) {
            throw new CreateReviewFromDatabaseException(e);
        }
    }

}
