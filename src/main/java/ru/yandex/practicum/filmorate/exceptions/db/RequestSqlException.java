package ru.yandex.practicum.filmorate.exceptions.db;

public class RequestSqlException extends RuntimeException {

    public RequestSqlException(Throwable cause) {
        super(cause);
    }

    public RequestSqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestSqlException(String message) {
        super(message);
    }
}

