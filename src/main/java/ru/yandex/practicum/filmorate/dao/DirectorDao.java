package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorDao {

    List<Director> getDirectors();

    Director getDirectorById(int id);

    Director postDirector(Director director);

    Director putDirector(Director director);

    void deleteDirector(int id);

}
