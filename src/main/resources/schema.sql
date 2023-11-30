drop table mpa_rating IF EXISTS CASCADE;
drop table genres IF EXISTS CASCADE;
drop table movies IF EXISTS CASCADE;
drop table movies_genres IF EXISTS CASCADE;
drop table users IF EXISTS CASCADE;
drop table user_relations IF EXISTS CASCADE;
drop table movies_likes IF EXISTS CASCADE;

create TABLE IF NOT EXISTS mpa_rating (
    rating_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT ''
);

create TABLE IF NOT EXISTS genres (
    genre_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL
);

create TABLE IF NOT EXISTS movies (
    movie_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description VARCHAR DEFAULT '',
    release_date DATE,
    duration INT,
    rating INT references mpa_rating(rating_id)
);

create TABLE IF NOT EXISTS movies_genres (
    movie_id INT REFERENCES movies(movie_id),
    genre_id INT REFERENCES genres(genre_id),
    PRIMARY KEY (movie_id, genre_id)
);

create TABLE IF NOT EXISTS users (
  user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email VARCHAR NOT NULL UNIQUE,
  login VARCHAR NOT NULL UNIQUE,
  name VARCHAR NOT NULL,
  birthday DATE
);

create TABLE IF NOT EXISTS user_relations (
    requester_id INT REFERENCES users(user_id),
    approver_id INT REFERENCES users(user_id),
    accepted boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY (requester_id, approver_id)
);

create TABLE IF NOT EXISTS movies_likes (
    movie_id INT REFERENCES movies(movie_id),
    user_id INT REFERENCES users(user_id),
    PRIMARY KEY (movie_id, user_id)
);