package ru.yandex.practicum.filmorate.exceptions.genre;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class GenreNotFoundException extends ItemNotFoundException {
  public GenreNotFoundException(int id) {
    super(String.format("Жанр с идентификатором %d не найден", id));
  }
}
