package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder(setterPrefix = "with")
public class Feed {
    @NotNull
    private Integer eventId;
    private Long timestamp;
    @NotNull
    private Integer userId;
    @NotNull
    private String eventType;
    @NotNull
    private String operation;
    @NotNull
    private Integer entityId;
}
