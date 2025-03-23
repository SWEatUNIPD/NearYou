DROP TYPE IF EXISTS category_enum CASCADE;

DROP TABLE IF EXISTS users cascade;
DROP TABLE IF EXISTS bikes cascade;
DROP TABLE IF EXISTS rents cascade;
DROP TABLE IF EXISTS points_of_interest cascade;
DROP INDEX IF EXISTS idx_points_of_interest_location;
DROP TABLE IF EXISTS advertisements cascade;
DROP TABLE IF EXISTS positions cascade;
DROP TABLE IF EXISTS user_interests CASCADE;
DROP TABLE IF EXISTS poi_hours CASCADE;

CREATE TABLE users
(
    email     TEXT NOT NULL PRIMARY KEY,
    name      TEXT NOT NULL,
    text_area TEXT
);

CREATE TYPE category_enum AS ENUM ('Ristorazione','Istruzione','Trasporti','Servizi finanziari','Sanità','Servizi pubblici',
    'Gestione dei rifiuti','Intrattenimento, Arte e Cultura', 'Strutture', 'Cibo e bevande',
    'Negozio generico, grande magazzino, centro commerciale','Abbigliamento, scarpe, accessori',
    'Negozio sconti, enti di beneficenza','Salute e bellezza','Fai da te, casalinghi, materiali edili, giardinaggio',
    'Arredamento e interni','Elettronica','Attività esterne, sport e veicoli', 'Arte, musica, hobby',
    'Cartoleria, regali, libri, giornali');

CREATE TABLE user_interests
(
    user_email TEXT          NOT NULL REFERENCES users (email) ON DELETE CASCADE,
    category   category_enum NOT NULL,
    PRIMARY KEY (user_email, category)
);

CREATE TABLE bikes
(
    id SERIAL PRIMARY KEY
);

CREATE TABLE rents
(
    id         SERIAL NOT NULL PRIMARY KEY,
    bike_id    INT    NOT NULL REFERENCES bikes (id) ON DELETE CASCADE,
    user_email TEXT   NOT NULL REFERENCES users (email) ON DELETE CASCADE
);

CREATE TABLE positions
(
    time_stamp TIMESTAMP WITH TIME ZONE,
    rent_id    INT  NOT NULL REFERENCES rents (id) ON DELETE CASCADE,
    latitude   REAL NOT NULL,
    longitude  REAL NOT NULL,
    PRIMARY KEY (time_stamp, rent_id)
);

CREATE TABLE points_of_interest
(
    latitude  REAL          NOT NULL,
    longitude REAL          NOT NULL,
    vat       CHAR(11)      NOT NULL,
    name      TEXT          NOT NULL,
    category  category_enum NOT NULL,
    offer     TEXT          NOT NULL,
    PRIMARY KEY (latitude, longitude)
);

CREATE INDEX idx_points_of_interest_location ON points_of_interest USING GIST (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)); --FIXME: is it useful?

CREATE TABLE poi_hours
(
    latitude_poi  REAL                NOT NULL,
    longitude_poi REAL                NOT NULL,
    day_of_week   INT                 NOT NULL CHECK (day_of_week <= 7 AND day_of_week >= 1),
    open_at       TIME WITH TIME ZONE NOT NULL,
    close_at      TIME WITH TIME ZONE NOT NULL,
    CHECK (close_at > open_at),
    FOREIGN KEY (latitude_poi, longitude_poi) REFERENCES points_of_interest (latitude, longitude),
    PRIMARY KEY (latitude_poi, longitude_poi, day_of_week)
);

CREATE TABLE advertisements
(
    id                  SERIAL                   NOT NULL PRIMARY KEY,
    latitude_poi        REAL                     NOT NULL,
    longitude_poi       REAL                     NOT NULL,
    position_time_stamp TIMESTAMP WITH TIME ZONE NOT NULL,
    position_rent_id    INT,
    adv                 TEXT,
    FOREIGN KEY (latitude_poi, longitude_poi) REFERENCES points_of_interest (latitude, longitude) ON DELETE CASCADE,
    FOREIGN KEY (position_time_stamp, position_rent_id) REFERENCES positions (time_stamp, rent_id),
    UNIQUE (latitude_poi, longitude_poi, position_rent_id)
);


INSERT INTO "bikes" ("id") VALUES
(1);

INSERT INTO "points_of_interest" ("latitude", "longitude", "vat", "name", "category", "offer") VALUES
(78.5, 78.5, 'IT101010101', 'Cafè Zhivago', 'Cibo e bevande', 'Drink, superalcolici, aperitivi di laurea, birre artigianali');

INSERT INTO "poi_hours" ("latitude_poi", "longitude_poi", "day_of_week", "open_at", "close_at") VALUES
(78.5, 78.5, 1, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 2, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 3, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 4, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 5, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 6, '00:00:00+00', '23:59:59+00'),
(78.5, 78.5, 7, '00:00:00+00', '23:59:59+00');

INSERT INTO "users" ("email", "name", "text_area") VALUES
('utente1@example.com', 'Ingegneria Delsudore', 'Birre artigianli, drink delicati, whisky, rum');

INSERT INTO "user_interests" ("user_email", "category") VALUES
('utente1@example.com', 'Cibo e bevande');

INSERT INTO "rents" ("id", "bike_id", "user_email") VALUES
(1, 1, 'utente1@example.com');