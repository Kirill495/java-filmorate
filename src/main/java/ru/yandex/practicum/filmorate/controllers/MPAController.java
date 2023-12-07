package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/mpa")
public class MPAController {

    private final MPAService mpaService;

    public MPAController(MPAService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<MPA> getAllRatings() {
        log.info("Getting all ratings");
        return mpaService.getAllRating();
    }

    @GetMapping("/{id}")
    public MPA getRatingById(@PathVariable("id") int id) {
        log.info("Getting rating with id:{}", id);
        return mpaService.getRatingById(id);
    }
}
