-- Abilita le funzioni per generare UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Rimuove eventuale definizione pregressa dell'enum
DROP TYPE IF EXISTS category_enum CASCADE;

-- Ricrea l'enum per le categorie
CREATE TYPE category_enum AS ENUM (
    'food',
    'clothes',
    'other'
);

-- Drop delle tabelle precedenti
DROP TABLE IF EXISTS advertisements CASCADE;
DROP TABLE IF EXISTS points_of_interest CASCADE;
DROP TABLE IF EXISTS merchants CASCADE;
DROP TABLE IF EXISTS positions CASCADE;
DROP TABLE IF EXISTS rents CASCADE;
DROP TABLE IF EXISTS bikes CASCADE;
DROP TABLE IF EXISTS user_categories CASCADE;  -- nuova tabella che rimuoviamo se esiste
DROP TABLE IF EXISTS users CASCADE;

-- Tabella users, con campo 'preferences' (max 200 char). 
-- Rimossa la colonna categories[] (ora gestita da 'user_categories')
CREATE TABLE IF NOT EXISTS users (
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    name TEXT NOT NULL,
    preferences VARCHAR(200)
);

-- Nuova tabella per la relazione 1:N tra users e categories
-- Un utente può avere più righe, ognuna con la propria categoria
CREATE TABLE IF NOT EXISTS user_categories (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category category_enum NOT NULL,
    PRIMARY KEY (user_id, category)
);

-- Tabella bikes invariata
CREATE TABLE IF NOT EXISTS bikes(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY
);

-- Tabella rents invariata
CREATE TABLE IF NOT EXISTS rents(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    bike_id UUID NOT NULL REFERENCES bikes(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_closed BOOL NOT NULL DEFAULT FALSE
);

-- Tabella positions invariata
CREATE TABLE IF NOT EXISTS positions(
    id UUID NOT NULL DEFAULT uuid_generate_v4() PRIMARY KEY,
    rent_id UUID NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

-- Tabella merchants invariata
CREATE TABLE IF NOT EXISTS merchants(
    vat CHARACTER VARYING(11) NOT NULL PRIMARY KEY,
    name TEXT NOT NULL
);

-- Tabella points_of_interest con una singola colonna di tipo enum 
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

-- Tabella advertisements invariata
CREATE TABLE IF NOT EXISTS advertisements (
    rent_id UUID NOT NULL REFERENCES rents(id) ON DELETE CASCADE,
    poi_id  UUID NOT NULL REFERENCES points_of_interest(id) ON DELETE CASCADE,
    adv TEXT,
    PRIMARY KEY (rent_id, poi_id)
);
