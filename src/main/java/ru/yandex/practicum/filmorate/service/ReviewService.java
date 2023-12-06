package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

        feedService.postEvent(Feed.builder().setUserId(review.getUserId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .setEventType(EventType.REVIEW.toString())
                .setOperation(Operation.ADD.toString())
                .setEntityId(curReview.getReviewId())
                .build());
        return curReview;
    }

    public Review updateReview(Review review) {
        checkReviewFieldsConsistency(review);
        Review curReview = reviewStorage.updateReview(review);

        feedService.postEvent(Feed.builder().setUserId(review.getUserId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .setEventType(EventType.REVIEW.toString())
                .setOperation(Operation.UPDATE.toString())
                .setEntityId(review.getReviewId())
                .build());
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
        feedService.postEvent(Feed.builder().setUserId(review.getUserId())
                .setTimestamp(Timestamp.valueOf(LocalDateTime.now()).getTime())
                .setEventType(EventType.REVIEW.toString())
                .setOperation(Operation.REMOVE.toString())
                .setEntityId(review.getReviewId())
                .build());
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
            throw new UserNotFoundException(review.getUserId());
        }
    }
}
