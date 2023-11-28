package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewEstimation {
    private int reviewId;
    private int userId;
    private boolean isUseful;
}
