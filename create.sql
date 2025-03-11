CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS category_enum CASCADE;

CREATE TYPE category_enum AS ENUM (
    'food',
    'clothes',
    'other'
);

DROP TABLE IF EXISTS users cascade;
DROP TABLE IF EXISTS bikes cascade;
DROP TABLE IF EXISTS rents cascade;
DROP TABLE IF EXISTS merchants cascade;
DROP TABLE IF EXISTS points_of_interest cascade;
DROP TABLE IF EXISTS advertisements cascade;
DROP TABLE IF EXISTS positions cascade;
DROP TABLE IF EXISTS user_categories CASCADE;

CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    name TEXT NOT NULL,
    preferences VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS user_categories (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category category_enum NOT NULL,
    PRIMARY KEY (user_id, category)
);

CREATE TABLE IF NOT EXISTS bikes(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS rents(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    bike_id UUID NOT NULL REFERENCES bikes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_closed BOOL NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS positions(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    rent_id UUID NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS merchants(
    vat CHARACTER VARYING(11) NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS points_of_interest (
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    merchant_vat CHARACTER VARYING(11) NOT NULL REFERENCES merchants(vat) ON DELETE CASCADE,
    name TEXT NOT NULL,
    start_at TIME NOT NULL,
    end_at TIME NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    category category_enum NOT NULL
);

CREATE TABLE IF NOT EXISTS advertisements (
    rent_id UUID NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    poi_id  UUID NOT NULL REFERENCES points_of_interest(id) ON DELETE CASCADE,
    adv TEXT,
    PRIMARY KEY (rent_id, poi_id)
);