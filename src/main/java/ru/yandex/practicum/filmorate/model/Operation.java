package ru.yandex.practicum.filmorate.model;

import java.util.HashMap;
import java.util.Map;

public enum Operation {
    REMOVE(1),
    ADD(2),
    UPDATE(3);

    public final Integer operationId;

    private static final Map<Operation, Integer> BY_INTEGER_NUMBER = new HashMap<>();

    static {
        for (Operation e : values()) {
            BY_INTEGER_NUMBER.put(e, e.operationId);
        }
    }

    Operation(Integer operationId) {
        this.operationId = operationId;
    }

    public static int getEventTypeId(Operation operation) {
        return BY_INTEGER_NUMBER.get(operation);
    }
}
