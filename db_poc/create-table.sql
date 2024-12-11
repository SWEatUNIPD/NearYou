-- DROP

drop table if exists UserProfile cascade;
drop table if exists Merchant cascade;
drop table if exists Subscription cascade;
drop table if exists Sensor cascade;
drop table if exists SensorInUse cascade;
drop table if exists SensorLocation cascade;
drop table if exists PointOfInterest cascade;
drop table if exists Interest cascade;
drop table if exists Partnership cascade;

drop type if exists gender_type;

-- CREATE

create type gender_type as enum ('male', 'female', 'not_specified');

create table UserProfile (
    email varchar(50) primary key,
    name varchar(15) not null,
    surname varchar(15) not null,
    gender gender_type not null,
    age smallint not null
);

create table Merchant (
    piva char(11) primary key,
    email varchar(50) not null,
    activity_name varchar(30) not null
);

create table Subscription (
    user_email varchar(50) references UserProfile(email) on update cascade on delete no action,
    merchant_piva char(11) references Merchant(piva) on update cascade on delete no action,
    primary key (user_email, merchant_piva)
);

create table Sensor (
    id bigint primary key,   -- univoke ID within the enteir system, independently from the merchant
    merchant_piva char(11) not null references Merchant(piva) on update cascade on delete set null
);

create table SensorInUse (
    sensor_id bigint references Sensor(id) on update cascade on delete no action,
    start_time timestamp not null,
    primary key (sensor_id, start_time),
    end_time timestamp,
    user_email varchar(50) not null references UserProfile(email) on update cascade on delete set null
    -- TODO: check (start_time < end_time) when end_time is not null
);

create table SensorLocation (
    sensor_id bigint references Sensor(id) on update cascade on delete no action,
    retrieved_time timestamp,
    primary key (sensor_id, retrieved_time),
    latitude decimal not null,
    longitude decimal not null
);

create table PointOfInterest (
    latitude decimal,
    longitude decimal,
    primary key (latitude, longitude),
    activity_name varchar(30) not null
);

create table Interest (
    sensor_id bigint references Sensor(id) on update cascade on delete no action,
    poi_latitude decimal,
    poi_longitude decimal,
    primary key (sensor_id, poi_latitude, poi_longitude),
    foreign key (poi_latitude, poi_longitude) references PointOfInterest(latitude, longitude) on update cascade on delete no action,
    trigger_time timestamp not null
);

create table Partnership (
    merchant_piva char(11) references Merchant(piva) on update cascade on delete no action,
    poi_latitude decimal,
    poi_longitude decimal,
    primary key (merchant_piva, poi_latitude, poi_longitude),
    foreign key (poi_latitude, poi_longitude) references PointOfInterest(latitude, longitude) on update cascade on delete no action
);
