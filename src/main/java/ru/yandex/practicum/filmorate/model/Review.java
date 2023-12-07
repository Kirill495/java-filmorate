package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder(setterPrefix = "with")
public class Review {
    private Integer reviewId;
    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;
    @NotNull
    private Boolean isPositive;
    @NotNull(message = "Автор отзыва должен быть указан")
    private Integer userId;
    @NotNull(message = "Не указан фильм в отзыве")
    private Integer filmId;
    private Integer useful;

    public Map<String, Object> toMap() {
        return Map.of("content", content,
                "isPositive", isPositive,
                "user_id", userId,
                "movie_id", filmId);
    }
}


