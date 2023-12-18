package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationsService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    private final RecommendationsService recommendationsService;

    @Autowired
    public UserController(UserService service, RecommendationsService recommendationsService) {
        this.service = service;
        this.recommendationsService = recommendationsService;
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Create new user with id:{}", user.getId());
        return service.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Update user with id:{}", user.getId());
        return service.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public boolean deleteUser(@PathVariable int userId) {
        log.info("Delete user with id:{}", userId);
        return service.deleteUser(userId);
    }

    @GetMapping
    public List<User> getUsers() {
        log.info("Get all users");
        return service.getUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return service.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Adding friend with id:{} to user with id:{}", friendId, id);
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        log.info("Deleting friend with id:{} from user with id:{}", friendId, id);
        service.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable int id) {
        log.info("Getting friends for user with id:{}", id);
        return service.getFriends(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getRecommendations(@PathVariable int id) {
        log.info("Getting recommendations for user with id:{}", id);
        return recommendationsService.getRecommendations(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Getting common friends for user with id:{} and otherId:{}", id, otherId);
        return service.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Feed> getFeed(@PathVariable("id") int userId) {
        log.info("Getting feed for user with id:{}", userId);
        return service.getFeed(userId);
    }

}
