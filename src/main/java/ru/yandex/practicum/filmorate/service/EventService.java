package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Service
public class EventService {

    private final EventDao eventStorage;

    @Autowired
    public EventService(EventDao eventStorage) {
        this.eventStorage = eventStorage;
    }

    public Event postEvent(Event event) {
        return eventStorage.postEvent(event);
    }

    public List<Event> getEvents(int userId) {
        return eventStorage.getEvents(userId);
    }
}
