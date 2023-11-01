package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class MPA {
  private Integer id;
  private String name;
  private String description;

  public MPA() {
  }
}
