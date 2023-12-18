package ru.yandex.practicum.filmorate.exceptions.review;

import org.springframework.dao.DataAccessException;

public class ReviewMarksRemoveException extends DataAccessException {
    public ReviewMarksRemoveException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
