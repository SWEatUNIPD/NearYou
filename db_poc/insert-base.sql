-- TRUNCATE

truncate UserProfile cascade;
truncate Merchant cascade;
truncate Subscription cascade;
truncate Sensor cascade;
truncate SensorInUse cascade;
truncate PointOfInterest cascade;
truncate Partnership cascade;

-- INSERT

insert into UserProfile values ('ali.baba@gmail.it', 'Alì', 'Babà', 'male', 23);

insert into Merchant values ('59864223571', 'super.bike@info.com', 'Super Bike Noleggio');

insert into Subscription values ('ali.baba@gmail.it', '59864223571');

insert into Sensor values (0, '59864223571');
insert into Sensor values (1, '59864223571');

-- TODO: start_time does not correspond to the retrieved gpx
insert into SensorInUse values (1, '2024-12-13 14:05:06', null, 'ali.baba@gmail.it');

-- SensorLocation

insert into PointOfInterest values (45.41669, 11.8654, 'La Bottega delle Meraviglie');

-- Interest

insert into Partnership values ('59864223571', 45.41669, 11.8654);
