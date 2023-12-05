package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
public class Event {
    @NotNull
    Integer eventId;
    ZonedDateTime eventTime;
    @NotNull
    Integer userId;
    @NotNull
    String eventType;
    @NotNull
    String operation;
    @NotNull
    Integer entityId;
}
