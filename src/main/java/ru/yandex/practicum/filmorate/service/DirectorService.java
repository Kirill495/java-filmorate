package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.DirectorDao;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
public class DirectorService {

    private final DirectorDao directorDao;

    @Autowired
    public DirectorService(DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

    public List<Director> getDirectors() {
        return directorDao.getDirectors();
    }


    public Director getDirectorById(int id) {
        return directorDao.getDirectorById(id);
    }


    public Director postDirector(Director director) {
        return directorDao.postDirector(director);
    }

    public Director putDirector(Director director) {
        return directorDao.putDirector(director);
    }

    public void deleteDirector(int id) {
        directorDao.deleteDirector(id);
    }
}
