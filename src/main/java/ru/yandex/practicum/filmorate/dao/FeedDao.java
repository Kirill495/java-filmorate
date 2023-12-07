package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedDao {

    Feed postEvent(Feed feed);

    List<Feed> getEvents(int userId);
}
