package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
public class Director {
    private Integer id;
    @NotBlank(message = "Name may not be blank")
    @Size(min = 1, max = 100, message = "Name may not be empty or to large")
    private String name;
}
