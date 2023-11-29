package ru.yandex.practicum.filmorate.controllers;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;

import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return service.addReview(review);
    }

    @GetMapping("{id}")
    public Review getReview(@PathVariable("id") int id) {
        return service.getReview(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId, @RequestParam(defaultValue = "10") int count) {
        return service.getReviews((filmId == null) ? 0 : filmId, count);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return service.updateReview(review);
    }

    @DeleteMapping("{id}")
    public void removeReview(@PathVariable int id) {
        service.removeReview(id);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeToReview(@PathVariable(name = "id") int reviewId, @PathVariable(name = "userId") int userId) {
        service.addMarkToReview(reviewId, userId, 1);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDisLikeToReview(@PathVariable(name = "id") int reviewId, @PathVariable(name = "userId") int userId) {
        service.addMarkToReview(reviewId, userId, -1);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLikeToReview(@PathVariable(name = "id") int reviewId, @PathVariable(name = "userId") int userId) {
        service.removeMarkOfReview(reviewId, userId, 1);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeMarkOfReview(@PathVariable(name = "id") int reviewId, @PathVariable(name = "userId") int userId) {
        service.removeMarkOfReview(reviewId, userId, -1);
    }

}
