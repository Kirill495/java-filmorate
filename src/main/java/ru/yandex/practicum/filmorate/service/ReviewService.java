package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Operation;
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
    private final FeedService feedService;

    @Autowired
    public ReviewService(
            ReviewStorage reviewStorage,
            FeedService feedService,
            @Qualifier("FilmDbStorage") FilmStorage filmStorage,
            @Qualifier("UserDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.feedService = feedService;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review addReview(Review review) {
        checkReviewFieldsConsistency(review);
        Review curReview = reviewStorage.addReview(review);
        feedService.postEvent(review.getUserId(), curReview, Operation.ADD);
        return curReview;
    }

    public Review updateReview(Review review) {
        checkReviewFieldsConsistency(review);
        Review curReview = reviewStorage.updateReview(review);
        feedService.postEvent(curReview.getUserId(), curReview, Operation.UPDATE);
        return curReview;
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

    public void removeReview(int reviewId) {
        Review review = reviewStorage.getReview(reviewId);
        reviewStorage.removeReview(reviewId);
        feedService.postEvent(review.getUserId(), review, Operation.REMOVE);
    }

    public void addMarkToReview(int reviewId, int userId, int mark) {
        if (userStorage.getUser(userId) == null) {
            throw new UserNotFoundException(userId);
        }
        reviewStorage.addMarkToReview(reviewId, userId, mark);
    }

    public void removeMarkOfReview(int reviewId, int userId, int mark) {
        reviewStorage.getReview(reviewId);
        reviewStorage.removeMarkOfReview(reviewId, userId, mark);
    }

    private void checkReviewFieldsConsistency(Review review) {
        if (filmStorage.getFilm(review.getFilmId()) == null) {
            throw new FilmNotFoundException(review.getFilmId());
        }
        if (userStorage.getUser(review.getUserId()) == null) {
            throw new UserNotFoundException(review.getUserId());
        }
    }
}
