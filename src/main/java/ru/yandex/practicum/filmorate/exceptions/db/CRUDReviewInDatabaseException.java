package ru.yandex.practicum.filmorate.exceptions.db;

import org.springframework.dao.DataAccessException;

public class CRUDReviewInDatabaseException extends DataAccessException {
    public CRUDReviewInDatabaseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
