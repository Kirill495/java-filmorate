DROP TABLE IF EXISTS  movies_genres;
DROP TABLE IF EXISTS  MOVIES_LIKES;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS USER_RELATIONS;
DROP TABLE IF EXISTS reviews_estimations;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_user_movie_unique;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_user_not_null;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_movie_not_null;
DROP TABLE IF EXISTS reviews;
drop table if exists users;
drop table if exists movies;
drop table if exists mpa_rating;

CREATE TABLE IF NOT EXISTS mpa_rating (
    rating_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT ''
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
    movie_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT '',
    release_date DATE,
    duration INT,
    rating INT references mpa_rating(rating_id)
);

CREATE TABLE IF NOT EXISTS movies_genres (
    movie_id INT REFERENCES movies(movie_id),
    genre_id INT REFERENCES genres(genre_id),
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users (
  user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email VARCHAR NOT NULL UNIQUE,
  login VARCHAR NOT NULL UNIQUE,
  name VARCHAR NOT NULL,
  birthday DATE
);

CREATE TABLE IF NOT EXISTS user_relations (
    requester_id INT REFERENCES users(user_id),
    approver_id INT REFERENCES users(user_id),
    accepted boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY (requester_id, approver_id)
);

CREATE TABLE IF NOT EXISTS movies_likes (
    movie_id INT REFERENCES movies(movie_id),
    user_id INT REFERENCES users(user_id),
    PRIMARY KEY (movie_id, user_id)
);

CREATE TABLE IF NOT EXISTS reviews
(
    review_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content    VARCHAR(MAX),
    isPositive BOOLEAN,
    user_id    INT REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    movie_id   INT REFERENCES movies (movie_id) ON DELETE CASCADE ON UPDATE CASCADE
);
alter table reviews add constraint reviews_user_not_null CHECK user_id is NOT NULL;
alter table reviews add constraint reviews_movie_not_null CHECK movie_id is NOT NULL;
alter table reviews add constraint reviews_user_movie_unique UNIQUE(user_id, movie_id);

CREATE TABLE IF NOT EXISTS reviews_estimations
(
    review_id INT REFERENCES reviews (review_id) ON DELETE CASCADE ON UPDATE CASCADE,
    user_id   INT REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    mark      INT,
    PRIMARY KEY (review_id, user_id),
    CHECK (mark = 1 OR mark = -1)
);