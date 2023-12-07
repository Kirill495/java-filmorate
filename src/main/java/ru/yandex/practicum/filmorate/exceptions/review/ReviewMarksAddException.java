package ru.yandex.practicum.filmorate.exceptions.review;

import org.springframework.dao.DataAccessException;

public class ReviewMarksAddException extends DataAccessException {
    public ReviewMarksAddException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
