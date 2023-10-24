package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
  private int id;
  @Email(message = "это не e-mail")
  private String email;
  @NotBlank(message = "Логин не должен быть пустым")
  @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
  private String login;
  private String name;
  @PastOrPresent(message = "Дата рождения не может быть больше текущей даты")
  private LocalDate birthday;
  private Set<Integer> friends = new HashSet<>();
}
