INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Yanislav', 'Dimitrov', 'yanidim', 'User1', 'yanislav.dimitrov@gmail.com', '+359899220228', 'ADMIN', 'ACTIVE');
INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Ivan', 'Boev', 'b0ew', 'User1', 'Ivan.Boev@gmail.com', '+359899220229', 'USER', 'ACTIVE');
INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Stamat', 'Ivanov', 'stamo13', 'User1', 'Stamat.Ivanov@gmail.com', '+359899220227', 'USER', 'ACTIVE');
INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Pesho', 'Dimitrov', 'peshkata90', 'User1', 'Pesho.Dimitrov@gmail.com', '+359899220225', 'USER', 'ACTIVE');
INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Anton', 'Kirilov', 'tonkata1', 'User1', 'Anton.Kirilov@gmail.com', '+359899220226', 'USER', 'ACTIVE');
INSERT INTO `carpooling`.`users` (`first_name`, `last_name`, `username`, `password`, `email`, `phone_number`, `role`,
                                  `status`)
VALUES ('Hrist', 'Hristov', 'icaka94', 'User1', 'Hristo.Hristov@gmail.com', '+359899220222', 'USER', 'ACTIVE');

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('BMW','E90','A1873HK','Saloon','Black','2010',1);

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('Audi','Q7','A4355HK','SUV','Black','2016',2);

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('Audi','S8','A4444PA','Saloon','Black','2023',3);

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('BMW','X6','A5511HA','SUV','Black','2022',4);

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('Mercedes','SL','PB0743CM','Saloon','White','2021',5);

insert into `carpooling`.vehicles(make, model, licence_plate_number, type, color, year_of_production, owner_id)
values ('Toyota','Land Cruiser','CA1111HA','SUV','Black','2023',6);


insert into `carpooling`.travels (free_spots, departure_time, comment, driver_id, vehicle_id, status, distance, departure_point, arrival_point)
values ('3',now(),'No dogs in the car!',1,1,'ACTIVE',100,"str.Vasil Levski 31,Aytos,Burgas,Bulgaria","str.Vasil Drumev 73Varna,Bulgaria");
insert into `carpooling`.travels (free_spots, departure_time, comment, driver_id, vehicle_id, status, distance, departure_point, arrival_point)
values ('4',now(),'No smoking in the car!',2,2,'ACTIVE',150,"str.Bogoridi 19,Aytos,Burgas,Bulgaria","str.Tsarevets 8 Varna Bulgaria");
insert into `carpooling`.travels (free_spots, departure_time, comment, driver_id, vehicle_id, status, distance, departure_point, arrival_point)
values ('2',now(),'No chalga music in the car!',3,3,'ACTIVE',350,"str.Vasil Levski 31,Aytos,Burgas,Bulgaria","str.Yanaki Stoilov 43,Sofia,Bulgaria");
insert into `carpooling`.travels (free_spots, departure_time, comment, driver_id, vehicle_id, status, distance, departure_point, arrival_point)
values ('3',now(),'No dogs in the car!',4,4,'ACTIVE',240,"str.Vasil Levski 31,Burgas,Bulgaria","str.Hristo Smirnenski 9 Plovdiv,Bulgaria");
insert into `carpooling`.travels (free_spots, departure_time, comment, driver_id, vehicle_id, status, distance, departure_point, arrival_point)
values ('4',now(),'No dogs in the car!',5,5,'ACTIVE',280,"str.Vasil Levski 31,Aytos,Burgas,Bulgaria","str.Buzludzha 73 Ruse,Bulgaria");