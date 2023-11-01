package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserRelation {
    private int requesterId;
    private int approverId;
    private boolean accepted;

    public UserRelation() {
    }

}
