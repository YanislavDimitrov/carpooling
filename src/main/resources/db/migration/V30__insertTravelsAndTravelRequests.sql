insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('BMW','E90','A1873HK','Saloon','BLACK','2010',1,0);
insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('Mercedes','G63 AMG','A8888HA','SUV','BLACK','2023',2,0);
insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('BMW','M4','A1111PB','Saloon','BLACK','2023',3,0);
insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('Audi','SQ7','A4833HK','SUV','BLACK','2022',4,0);
insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('Audi','SQ7','A4833HK','SUV','BLACK','2022',4,0);
insert into carpooling.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id, is_deleted)
VALUES ('Mercedes','GLS','A5533PA','SUV','BLACK','2023',5,0);
#
# insert into carpooling.travels(free_spots, departure_time, comment, driver_id, vehicle_id, status, departure_point, arrival_point, distance, duration, arrival_time)
# values (4,now(),'No smoking in the car',1,1,'ACTIVE','Sofia','Burgas','368km','3hours 54 minutes','2023-12-15 23:50:26');
# insert into carpooling.travels(free_spots, departure_time, comment, driver_id, vehicle_id, status, departure_point, arrival_point, distance, duration, arrival_time)
# values (3,now(),'No pets in the car',2,2,'ACTIVE','Aytos','Burgas','33km',' 30 minutes','2023-12-15 23:50:26');
# insert into carpooling.travels(free_spots, departure_time, comment, driver_id, vehicle_id, status, departure_point, arrival_point, distance, duration, arrival_time)
# values (2,now(),'No chalga in the car',3,3,'ACTIVE','Burgas','Varna','140km','1hours 54 minutes','2023-12-15 23:50:26');
# insert into carpooling.travels(free_spots, departure_time, comment, driver_id, vehicle_id, status, departure_point, arrival_point, distance, duration, arrival_time)
# values (1,now(),'No horses in the car',4,4,'ACTIVE','Sofia','Berkovitsa','86km','1hours 14 minutes','2023-12-15 23:50:26');
# insert into carpooling.travels(free_spots, departure_time, comment, driver_id, vehicle_id, status, departure_point, arrival_point, distance, duration, arrival_time)
# values (4,now(),'No eating in the car',5,5,'ACTIVE','Dobrich','Ruse','230km','2hours 14 minutes','2023-12-15 23:50:26');

insert into carpooling.travel_requests(user_id, travel_id, status)
values (1,1,'PENDING');
insert into carpooling.travel_requests(user_id, travel_id, status)
values (2,2,'PENDING');
insert into carpooling.travel_requests(user_id, travel_id, status)
values (3,3,'PENDING');
insert into carpooling.travel_requests(user_id, travel_id, status)
values (4,4,'PENDING');
insert into carpooling.travel_requests(user_id, travel_id, status)
values (5,5,'PENDING');

