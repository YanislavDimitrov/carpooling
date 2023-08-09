CREATE SCHEMA IF NOT EXISTS `carpooling`;
USE `carpooling`;
create table coordinates
(
    coordinate_id int auto_increment
        primary key,
    latitude      varchar(30) not null,
    longitude     varchar(30) not null
);

create table users
(
    user_id      int auto_increment
        primary key,
    first_name   varchar(20)                           not null,
    last_name    varchar(20)                           not null,
    username     varchar(20)                           not null,
    password     varchar(30)                           not null,
    email        varchar(30)                           not null,
    phone_number varchar(15)                           not null,
    role         enum ('ADMIN', 'USER')                not null,
    status       enum ('ACTIVE', 'BLOCKED', 'DELETED') not null,
    constraint users_pk2
        unique (username),
    constraint users_pk3
        unique (email),
    constraint users_pk4
        unique (phone_number)
);

create table vehicles
(
    vehicle_id           int auto_increment
        primary key,
    maker                varchar(20)                                                       not null,
    model                varchar(20)                                                       not null,
    licence_plate_number varchar(20)                                                       not null,
    type                 enum ('SUV', 'Saloon', 'Hatchback', 'Cabriolet', 'Station Wagon') not null,
    color                varchar(20)                                                       not null,
    year_of_production   varchar(10)                                                       not null,
    user_id              int                                                               not null,
    constraint vehicles_users_user_id_fk
        foreign key (user_id) references users (user_id)
);

create table travels
(
    travel_id      int auto_increment
        primary key,
    start_point    int          not null,
    end_point      int          not null,
    free_spots     smallint     not null,
    departure_time datetime     not null,
    comment        varchar(300) null,
    user_id        int          null,
    vehicle_id     int          null,
    status         tinyint(1)   null,
    constraint travel_coordinates_coordinate_id_fk
        foreign key (start_point) references coordinates (coordinate_id),
    constraint travel_coordinates_coordinate_id_fk2
        foreign key (end_point) references coordinates (coordinate_id),
    constraint travel_users_user_id_fk
        foreign key (user_id) references users (user_id),
    constraint travel_vehicles_vehicle_id_fk
        foreign key (vehicle_id) references vehicles (vehicle_id)
);

create table feedbacks
(
    feedback_id int auto_increment
        primary key,
    user_id     int      not null,
    travel_id   int      not null,
    comment     text     null,
    rating      smallint null,
    constraint feedback_travels_travel_id_fk
        foreign key (travel_id) references travels (travel_id),
    constraint feedback_users_user_id_fk
        foreign key (user_id) references users (user_id),
    constraint unique_user_id_travel_id
        unique (user_id, travel_id)
);

