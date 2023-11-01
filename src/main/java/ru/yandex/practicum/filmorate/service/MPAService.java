package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MPADao;
import ru.yandex.practicum.filmorate.model.MPA;


import java.util.List;

@Service
public class MPAService {

  private final MPADao MPADao;

  @Autowired
  public MPAService(MPADao mpaDao) {
    this.MPADao = mpaDao;
  }

  public List<MPA> getAllRating() {
    return MPADao.findAllMPARating();
  }

  public MPA getRatingById(int id) {
    return MPADao.findMPARatingById(id);
  }
}
