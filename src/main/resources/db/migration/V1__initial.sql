CREATE TABLE `users`
(
    `id` int(11) PRIMARY KEY AUTO_INCREMENT,
    `first_name` varchar(20) NOT NULL,
    `last_name` varchar(20) NOT NULL,
    `username` varchar(20) NOT NULL UNIQUE,
    `password` varchar(30) NOT NULL,
    `email` varchar(30) NOT NULL UNIQUE,
    `phone_number` varchar(15) NOT NULL UNIQUE,
    `role` enum ('ADMIN','USER') NOT NULL,
    `status` enum ('ACTIVE','BLOCKED','DELETED') NOT NULL
);

CREATE TABLE `vehicles`
(
    `id` int(11) PRIMARY KEY AUTO_INCREMENT,
    `make` varchar(20) NOT NULL,
    `model` varchar(20) NOT NULL,
    `licence_plate_number` varchar(20) NOT NULL,
    `type` enum ('SUV','Saloon','Hatchback','Cabriolet','Station Wagon') NOT NULL,
    `color` varchar(20) NOT NULL,
    `year_of_production` varchar(10) NOT NULL,
    `owner_id` int(11) NOT NULL,
    CONSTRAINT `vehicles_users_user_id_fk` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
);

CREATE TABLE `travels`
(
    `id` int(11) PRIMARY KEY AUTO_INCREMENT,
    `free_spots` smallint(6) NOT NULL,
    `departure_time` datetime NOT NULL,
    `comment` varchar(300) DEFAULT NULL,
    `driver_id` int(11) DEFAULT NULL,
    `vehicle_id` int(11) DEFAULT NULL,
    `status` enum ('ACTIVE','COMPLETED') NOT NULL,
    `start_latitude` double NOT NULL,
    `start_longitude` double NOT NULL,
    `end_latitude` double NOT NULL,
    `end_longitude` double NOT NULL,
    CONSTRAINT `travel_users_user_id_fk` FOREIGN KEY (`driver_id`) REFERENCES `users` (`id`),
    CONSTRAINT `travel_vehicles_vehicle_id_fk` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicles` (`id`)
);

CREATE TABLE `users_travels`
(
    `id` int(11) PRIMARY KEY AUTO_INCREMENT,
    `user_id` int(11) NOT NULL,
    `travel_id` int(11) NOT NULL,
    CONSTRAINT `fk_users_travels_travels` FOREIGN KEY (`travel_id`) REFERENCES `travels` (`id`),
    CONSTRAINT `fk_users_travels_users` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);


CREATE TABLE `feedbacks`
(
    `id` int(11) PRIMARY KEY AUTO_INCREMENT,
    `creator_id` int(11) NOT NULL,
    `travel_id` int(11) NOT NULL,
    `comment` text,
    `rating` smallint(6) DEFAULT NULL,
    UNIQUE KEY `unique_user_id_travel_id` (`creator_id`, `travel_id`),
    CONSTRAINT `feedback_travels_travel_id_fk` FOREIGN KEY (`travel_id`) REFERENCES `travels` (`id`),
    CONSTRAINT `feedback_users_user_id_fk` FOREIGN KEY (`creator_id`) REFERENCES `users` (`id`)
);