package ru.yandex.practicum.filmorate.exceptions.director;

import ru.yandex.practicum.filmorate.exceptions.ItemNotFoundException;

public class DirectorNotFoundException extends ItemNotFoundException {

    public DirectorNotFoundException(int id) {
        super(String.format("Директор с идентификатором %d не найден", id));
    }
}
