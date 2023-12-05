package ru.yandex.practicum.filmorate.model;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);

    private static final Map<EventType, Integer> BY_INTEGER_NUMBER = new HashMap<>();

    static {
        for (EventType e : values()) {
            BY_INTEGER_NUMBER.put(e, e.eventTypeID);
        }
    }
    public final Integer eventTypeID;

    EventType(Integer eventTypeID) {
        this.eventTypeID = eventTypeID;
    }

    public static int getEventTypeId(EventType eventType) {
        return BY_INTEGER_NUMBER.get(eventType);
    }
}
