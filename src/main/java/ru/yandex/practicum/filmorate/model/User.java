package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Component
public class User {
  private int id;
  @NotNull(message = "e-mail не должен быть пустым")
  @Email(message = "это не e-mail")
  private String email;
  @NotNull(message = "Логин не должен быть пустым")
  @Pattern(regexp = "^\\S+$", message = "Логин не должен быть пустым или содержать пробелы")
  private String login;
  private String name;
  @PastOrPresent(message = "Дата рождения не может быть больше текущей даты")
  private LocalDate birthday;
  private Set<Integer> friends = new HashSet<>();
}
