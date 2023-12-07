package ru.yandex.practicum.filmorate.exceptions.feed;

public class FeedPostEventUnknownEntityException extends RuntimeException {
    public FeedPostEventUnknownEntityException(Object entity) {
        super("Неизвестная сущность: " + entity.getClass().getName());
    }
}
