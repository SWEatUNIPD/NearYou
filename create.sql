CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS category_enum CASCADE;

DROP TABLE IF EXISTS users cascade;
DROP TABLE IF EXISTS bikes cascade;
DROP TABLE IF EXISTS rents cascade;
DROP TABLE IF EXISTS merchants cascade;
DROP TABLE IF EXISTS points_of_interest cascade;
DROP INDEX IF EXISTS idx_points_of_interest_location;
DROP TABLE IF EXISTS advertisements cascade;
DROP TABLE IF EXISTS positions cascade;
DROP TABLE IF EXISTS user_interests CASCADE;
DROP TABLE IF EXISTS poi_hours CASCADE;

CREATE TABLE users
(
    id          SERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    preferences TEXT
);

CREATE TYPE category_enum AS ENUM ('restaurant','fast_food','bar','pub','cafe','clothes','shoes','other');

CREATE TABLE user_interests
(
    user_id  INT           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category category_enum NOT NULL,
    PRIMARY KEY (user_id, category)
);

CREATE TABLE bikes
(
    id SERIAL PRIMARY KEY
);

CREATE TABLE rents
(
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v1(),
    bike_id   INT  NOT NULL REFERENCES bikes (id) ON DELETE CASCADE,
    user_id   INT  NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    is_closed BOOL NOT NULL    DEFAULT FALSE
);

CREATE TABLE positions
(
    time_stamp TIMESTAMP WITH TIME ZONE,
    rent_id    UUID NOT NULL REFERENCES rents (id) ON DELETE CASCADE,
    latitude   REAL NOT NULL,
    longitude  REAL NOT NULL,
    PRIMARY KEY (time_stamp, rent_id)
);

CREATE TABLE merchants
(
    vat  CHAR(11) NOT NULL PRIMARY KEY,
    name TEXT     NOT NULL
);

CREATE TABLE points_of_interest
(
    id           SERIAL PRIMARY KEY,
    merchant_vat CHAR(11)      NOT NULL REFERENCES merchants (vat) ON DELETE CASCADE,
    name         TEXT          NOT NULL,
    latitude     REAL          NOT NULL,
    longitude    REAL          NOT NULL,
    category     category_enum NOT NULL,
    description  TEXT          NOT NULL
);

CREATE INDEX idx_points_of_interest_location ON points_of_interest USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326));

CREATE TABLE poi_hours
(
    poi_id      INT                 NOT NULL REFERENCES points_of_interest (id) ON DELETE CASCADE,
    day_of_week INT                 NOT NULL CHECK (day_of_week <= 7 AND day_of_week >= 1),
    open_at     TIME WITH TIME ZONE NOT NULL,
    close_at    TIME WITH TIME ZONE NOT NULL,
    CHECK (close_at > open_at),
    PRIMARY KEY (poi_id, day_of_week)
);

CREATE TABLE advertisements
(
    id                  SERIAL NOT NULL PRIMARY KEY,
    time_stamp_position TIMESTAMP WITH TIME ZONE,
    rent_id_position    UUID   NOT NULL,
    poi_id              INT    NOT NULL REFERENCES points_of_interest (id) ON DELETE CASCADE,
    adv                 TEXT,
    FOREIGN KEY (time_stamp_position, rent_id_position) REFERENCES positions (time_stamp, rent_id),
    UNIQUE (time_stamp_position, rent_id_position)
);
