package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder(setterPrefix = "set")
public class Feed {
    @NotNull
    Integer eventId;
    Long timestamp;
    @NotNull
    Integer userId;
    @NotNull
    String eventType;
    @NotNull
    String operation;
    @NotNull
    Integer entityId;
}
