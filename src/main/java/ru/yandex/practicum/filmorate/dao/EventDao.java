package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventDao {
    Event postEvent(Event event);
    List<Event> getEvents(int userId);
}
