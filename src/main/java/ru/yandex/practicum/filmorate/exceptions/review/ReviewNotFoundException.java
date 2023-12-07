package ru.yandex.practicum.filmorate.exceptions.review;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class ReviewNotFoundException extends ItemNotFoundException {
    public ReviewNotFoundException(int id) {
        this(String.format("Отзыв с id %d не найден.", id));
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }
}
