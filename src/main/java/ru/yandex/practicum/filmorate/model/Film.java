package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.validators.releaseFilmDate.ReleaseFilmDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Component
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
  private Set<User> likes = new HashSet<>();
  private Set<Genre> genres = new HashSet<>();
  private MPARating rating;
}
