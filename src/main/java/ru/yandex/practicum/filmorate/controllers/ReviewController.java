package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;

import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Adding review with id:{}", review.getReviewId());
        return service.addReview(review);
    }

    @GetMapping("{id}")
    public Review getReview(@PathVariable("id") @Positive(message = "{ReviewId.Positive}") int id) {
        log.info("Getting review with id:{}", id);
        return service.getReview(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) @Positive(message = "{FilmId.Positive}") Integer filmId,
            @RequestParam(defaultValue = "10") @Positive(message = "{Limit.Positive}") int count) {
        log.info("Getting {} reviews for film with id:{}", count, filmId);
        return service.getReviews((filmId == null) ? 0 : filmId, count);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Updating review with id:{}", review.getReviewId());
        return service.updateReview(review);
    }

    @DeleteMapping("{id}")
    public void removeReview(@PathVariable int id) {
        log.info("Removing review with id:{}", id);
        service.removeReview(id);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(
            @PathVariable(name = "id") @Positive(message = "{ReviewId.Positive}") int reviewId,
            @PathVariable(name = "userId") @Positive(message = "{UserId.Positive}") int userId) {
        log.info("Adding like from user with id:{} to review with id:{}", userId, reviewId);
        service.addMarkToReview(reviewId, userId, 1);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDisLikeToReview(
            @PathVariable(name = "id") @Positive(message = "{ReviewId.Positive}") int reviewId,
            @PathVariable(name = "userId") @Positive(message = "{UserId.Positive}") int userId) {
        log.info("Adding dislike from user with id:{} to review with id:{}", userId, reviewId);
        service.addMarkToReview(reviewId, userId, -1);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeToReview(
            @PathVariable(name = "id") @Positive(message = "{ReviewId.Positive}") int reviewId,
            @PathVariable(name = "userId") @Positive(message = "{UserId.Positive}") int userId) {
        log.info("Removing like from review with id:{} from user with id:{}", reviewId, userId);
        service.removeMarkOfReview(reviewId, userId, 1);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDisLikeOfReview(
            @PathVariable(name = "id") @Positive(message = "{ReviewId.Positive}") int reviewId,
            @PathVariable(name = "userId") @Positive(message = "{UserId.Positive}") int userId) {
        log.info("Removing mark of review with id:{} from user with id:{}", reviewId, userId);
        service.removeMarkOfReview(reviewId, userId, -1);
    }
}
