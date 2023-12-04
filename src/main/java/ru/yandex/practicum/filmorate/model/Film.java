package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.releaseFilmDate.ReleaseFilmDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
public class Film {
    private int id;
    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
    @Size(max = 200, message = "Описание фильма не может превышать {max} символов")
    private String description;
    @ReleaseFilmDate
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;
    @JsonIgnore
    private Set<Integer> likes = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private MPA mpa;
    private Set<Director> directors = new HashSet<>();

    public Map<String, Object> toMap() {
        return Map.of("title", name,
                "description", description,
                "release_date", releaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "duration", duration,
                "rating", mpa == null ? 0 : mpa.getId());
    }

    private List<String> directors = new ArrayList<>();
}
