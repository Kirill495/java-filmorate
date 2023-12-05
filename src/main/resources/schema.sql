DROP TABLE IF EXISTS reviews_estimations;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_user_movie_unique;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_user_not_null;
ALTER TABLE IF EXISTS reviews DROP CONSTRAINT reviews_movie_not_null;
DROP TABLE IF EXISTS reviews;
drop table mpa_rating IF EXISTS CASCADE;
drop table genres IF EXISTS CASCADE;
drop table movies IF EXISTS CASCADE;
drop table movies_genres IF EXISTS CASCADE;
drop table users IF EXISTS CASCADE;
drop table user_relations IF EXISTS CASCADE;
drop table movies_likes IF EXISTS CASCADE;

CREATE TABLE IF NOT EXISTS mpa_rating (
    rating_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT ''
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS directors (
	director_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
    movie_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT '',
    release_date DATE,
    duration INT,
    rating INT references mpa_rating(rating_id)
);

CREATE TABLE IF NOT EXISTS movies_directors (
    movie_id INT REFERENCES movies(movie_id) ON DELETE CASCADE,
    director_id INT REFERENCES directors(director_id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, director_id)
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
