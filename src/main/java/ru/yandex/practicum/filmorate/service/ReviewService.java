package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public ReviewService(
            ReviewStorage reviewStorage,
            @Qualifier("FilmDbStorage") FilmStorage filmStorage,
            @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review addReview(Review review) {
        checkReviewFieldsConsistency(review);
        return reviewStorage.addReview(review);
    }

    public Review getReview(int id) {
        return reviewStorage.getReview(id);
    }

    public List<Review> getReviews(int movieId, int count) {
        if (movieId != 0 && filmStorage.getFilm(movieId) == null) {
            throw new FilmNotFoundException(movieId);
        }
        return reviewStorage.getReviews(movieId, count);
    }

    public Review updateReview(Review review) {
        checkReviewFieldsConsistency(review);
        return reviewStorage.updateReview(review);
    }

    public void removeReview(int reviewId) {
        reviewStorage.removeReview(reviewId);
    }

    public void addMarkToReview(int reviewId, int userId, int mark) {
        reviewStorage.getReview(reviewId);
        if (userStorage.getUser(userId) == null) {
            throw new UserNotFoundException(userId);
        }
        reviewStorage.addMarkToReview(reviewId, userId, mark);
    }

    public void removeMarkOfReview(int reviewId, int userId, int mark) {
        reviewStorage.removeMarkOfReview(reviewId, userId, mark);
    }

    private void checkReviewFieldsConsistency(Review review) {
        if (filmStorage.getFilm(review.getFilmId()) == null) {
            throw new FilmNotFoundException(review.getFilmId());
        }
        if (userStorage.getUser(review.getUserId()) == null) {
            throw  new UserNotFoundException(review.getUserId());
        }
    }
}
