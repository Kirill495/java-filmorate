package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedDao;
import ru.yandex.practicum.filmorate.exceptions.feed.FeedPostEventUnknownEntityException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public class FeedService {

    private final FeedDao eventStorage;

    @Autowired
    public FeedService(FeedDao eventStorage) {
        this.eventStorage = eventStorage;
    }

    public void postEvent(int userId, Object entity, Operation operation) {
        EventType eventType;
        int entityId;

        if (entity instanceof User) {
            eventType = EventType.FRIEND;
            entityId = ((User) entity).getId();
        } else if (entity instanceof Review) {
            eventType = EventType.REVIEW;
            entityId = ((Review) entity).getReviewId();
        } else if (entity instanceof Film) {
            eventType = EventType.LIKE;
            entityId = ((Film) entity).getId();
        } else {
            throw new FeedPostEventUnknownEntityException(entity);
        }

        eventStorage.postEvent(Feed.builder()
                .withUserId(userId)
                .withEventType(eventType.toString())
                .withOperation(operation.toString())
                .withEntityId(entityId)
                .build());
    }

    public List<Feed> getEvents(int userId) {
        return eventStorage.getEvents(userId);
    }
}
