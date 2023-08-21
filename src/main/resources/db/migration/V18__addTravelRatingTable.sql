CREATE TABLE `travels_ratings`
(
    `id`        int(11) PRIMARY KEY AUTO_INCREMENT,
    `travel_id` int(11) NOT NULL,
    `user_id`   int(11) NOT NULL,
    `rating`    int(11) NOT NULL,

    foreign key (travel_id) references travels (id),
    foreign key (user_id) references users (id)
);