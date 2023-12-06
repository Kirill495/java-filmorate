package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedDao;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

@Service
public class FeedService {

    private final FeedDao eventStorage;

    @Autowired
    public FeedService(FeedDao eventStorage) {
        this.eventStorage = eventStorage;
    }

    public Feed postEvent(Feed feed) {
        return eventStorage.postEvent(feed);
    }

    public List<Feed> getEvents(int userId) {
        return eventStorage.getEvents(userId);
    }
}
