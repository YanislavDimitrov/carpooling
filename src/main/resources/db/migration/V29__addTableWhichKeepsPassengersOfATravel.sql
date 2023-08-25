CREATE TABLE `passengers_travels`
(
    `id`        int(11) PRIMARY KEY AUTO_INCREMENT,
    `passenger_id`   int(11) NOT NULL,
    `travel_id` int(11) NOT NULL,
    CONSTRAINT `fk_users_travels_travels` FOREIGN KEY (`travel_id`) REFERENCES `travels` (`id`),
    CONSTRAINT `fk_users_travels_users` FOREIGN KEY (`passenger_id`) REFERENCES `users` (`id`)
);

