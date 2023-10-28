package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class MPARating {
  private int id;
  private String title;
  private String description;
}
