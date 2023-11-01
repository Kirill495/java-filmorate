package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRelation {
    private int requesterId;
    private int approverId;
    private boolean accepted;

}
