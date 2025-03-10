DROP TABLE IF EXISTS users cascade;
DROP TABLE IF EXISTS bikes cascade;
DROP TABLE IF EXISTS rents cascade;
DROP TABLE IF EXISTS merchants cascade;
DROP TABLE IF EXISTS points_of_interest cascade;
DROP TABLE IF EXISTS advertisements cascade;
DROP TABLE IF EXISTS positions cascade;

CREATE TABLE IF NOT EXISTS users(
    id SERIAL NOT NULL PRIMARY KEY,
    name text NOT NULL
);

CREATE TABLE IF NOT EXISTS bikes(
    id SERIAL NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS rents(
    id SERIAL NOT NULL PRIMARY KEY,
    bike_id INT NOT NULL REFERENCES bikes(id) ON DELETE CASCADE,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_closed BOOL NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS positions(
    id SERIAL NOT NULL PRIMARY KEY,
    rent_id INT NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS merchants(
    vat CHARACTER VARYING(11) NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS points_of_interest(
    id SERIAL NOT NULL PRIMARY KEY,
    merchant_vat CHARACTER VARYING(11) NOT NULL REFERENCES merchants(vat) ON DELETE CASCADE,
    name TEXT NOT NULL,
    start_at TIME NOT NULL,
    end_at TIME NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS advertisements(
    rent_id INT NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    poi_id INT NOT NULL REFERENCES points_of_interest(id) ON DELETE CASCADE,
    adv TEXT,
    PRIMARY KEY (rent_id, poi_id)
);