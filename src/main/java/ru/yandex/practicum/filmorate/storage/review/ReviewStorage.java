package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    void removeReview(int id);

    Review getReview(int id);

    List<Review> getReviews(int quantity, int movieId);

    void addMarkToReview(int reviewId, int userId, int mark);

    void removeMarkOfReview(int reviewId, int userId, int mark);

}
