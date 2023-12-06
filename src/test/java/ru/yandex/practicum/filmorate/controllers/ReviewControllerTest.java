package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dao.impl.FeedDaoImpl;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureMockMvc
@SpringBootTest
public class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void testShouldGetTwoReviews() {

        FilmStorage filmStorage = new FilmDbStorage(jdbcTemplate);
        UserStorage userStorage = new UserDbStorage(jdbcTemplate);
        FeedService feedService = new FeedService(new FeedDaoImpl(new JdbcTemplate()));
        Film film1 = new Film();
        film1.setName("Film_1_");
        film1.setDescription("About film 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        MPA mpa = new MPA();
        mpa.setId(1);
        mpa.setName("G");
        mpa.setDescription("Нет возрастных ограничений");
        film1.setMpa(mpa);
        film1.setDuration(100);
        Film rFilm1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film_2_");
        film2.setDescription("About film 2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setMpa(mpa);
        film2.setDuration(200);
        Film rFilm2 = filmStorage.addFilm(film2);

        User user1 = new User();
        user1.setName("Username");
        user1.setLogin("UserLogin");
        user1.setEmail("User@email.org");
        user1.setBirthday(LocalDate.of(2000, 2, 2));
        User rUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setName("Username1");
        user2.setLogin("UserLogin1");
        user2.setEmail("User1@email.org");
        user2.setBirthday(LocalDate.of(2001, 2, 2));
        User rUser2 = userStorage.addUser(user2);

        ReviewStorage reviewStorage = new ReviewDbStorage(jdbcTemplate);
        ReviewService reviewService = new ReviewService(reviewStorage, feedService, filmStorage, userStorage);

        Review review1 = new Review();
        review1.setContent("Review 1 to film 1");
        review1.setUserId(rUser1.getId());
        review1.setFilmId(rFilm1.getId());
        review1.setIsPositive(true);
        review1 = reviewService.addReview(review1);

        Review review2 = new Review();
        review2.setContent("Review 2 to film 1");
        review2.setUserId(rUser2.getId());
        review2.setFilmId(rFilm1.getId());
        review2.setIsPositive(true);
        review2 = reviewService.addReview(review2);

        Review review3 = new Review();
        review3.setContent("Review 1 to film 2");
        review3.setUserId(rUser2.getId());
        review3.setFilmId(rFilm2.getId());
        review3.setIsPositive(false);
        review3 = reviewService.addReview(review3);

        List<Review> reviewList1 = reviewService.getReviews(rFilm1.getId(), 10);
        Assertions.assertEquals(2, reviewList1.size());
        Assertions.assertTrue(reviewList1.contains(review1));
        Assertions.assertTrue(reviewList1.contains(review2));

        List<Review> reviewList2 = reviewService.getReviews(0, 10);
        Assertions.assertEquals(3, reviewList2.size());
        Assertions.assertTrue(reviewList2.contains(review1));
        Assertions.assertTrue(reviewList2.contains(review2));
        Assertions.assertTrue(reviewList2.contains(review3));

    }
}
