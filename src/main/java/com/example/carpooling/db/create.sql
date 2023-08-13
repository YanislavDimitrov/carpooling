

create table users
(
    id           int auto_increment
        primary key,
    first_name   varchar(20)                           not null,
    last_name    varchar(20)                           not null,
    username     varchar(20)                           not null,
    password     varchar(30)                           not null,
    email        varchar(30)                           not null,
    phone_number varchar(15)                           not null,
    role         enum ('ADMIN', 'USER')                not null,
    status       enum ('ACTIVE', 'BLOCKED', 'DELETED') not null,
    constraint email
        unique (email),
    constraint phone_number
        unique (phone_number),
    constraint username
        unique (username)
);

create table vehicles
(
    id                   int auto_increment
        primary key,
    make                 varchar(20)                                                       not null,
    model                varchar(20)                                                       not null,
    licence_plate_number varchar(20)                                                       not null,
    type                 enum ('SUV', 'Saloon', 'Hatchback', 'Cabriolet', 'Station Wagon') not null,
    color                varchar(20)                                                       not null,
    year_of_production   varchar(10)                                                       not null,
    owner_id             int                                                               not null,
    constraint vehicles_users_user_id_fk
        foreign key (owner_id) references users (id)
);

create table travels
(
    id              int auto_increment
        primary key,
    free_spots      smallint                     not null,
    departure_time  datetime                     not null,
    comment         varchar(300)                 null,
    driver_id       int                          null,
    vehicle_id      int                          null,
    status          enum ('ACTIVE', 'COMPLETED') not null,
    distance        double                       not null,
    departure_point varchar(100)                 not null,
    arrival_point   varchar(100)                 null,
    constraint travel_users_user_id_fk
        foreign key (driver_id) references users (id),
    constraint travel_vehicles_vehicle_id_fk
        foreign key (vehicle_id) references vehicles (id)
);

create table feedbacks
(
    id           int auto_increment
        primary key,
    creator_id   int      not null,
    travel_id    int      not null,
    comment      text     null,
    rating       smallint null,
    recipient_id int      null,
    constraint unique_user_id_travel_id
        unique (creator_id, travel_id),
    constraint feedback_travels_travel_id_fk
        foreign key (travel_id) references travels (id),
    constraint feedback_users_user_id_fk
        foreign key (creator_id) references users (id),
    constraint feedbacks_users_id_fk
        foreign key (recipient_id) references users (id)
);

create table travel_requests
(
    id        int auto_increment
        primary key,
    user_id   int                                      not null,
    travel_id int                                      not null,
    status    enum ('PENDING', 'APPROVED', 'REJECTED') not null,
    constraint travel_requests_travels_id_fk
        foreign key (travel_id) references travels (id),
    constraint travel_requests_users_id_fk
        foreign key (user_id) references users (id)
);

create table users_travels
(
    id        int auto_increment
        primary key,
    user_id   int not null,
    travel_id int not null,
    constraint fk_users_travels_travels
        foreign key (travel_id) references travels (id),
    constraint fk_users_travels_users
        foreign key (user_id) references users (id)
);


create table users
(
    id           int auto_increment
        primary key,
    first_name   varchar(20)                           not null,
    last_name    varchar(20)                           not null,
    username     varchar(20)                           not null,
    password     varchar(30)                           not null,
    email        varchar(30)                           not null,
    phone_number varchar(15)                           not null,
    role         enum ('ADMIN', 'USER')                not null,
    status       enum ('ACTIVE', 'BLOCKED', 'DELETED') not null,
    constraint email
        unique (email),
    constraint phone_number
        unique (phone_number),
    constraint username
        unique (username)
);

create table vehicles
(
    id                   int auto_increment
        primary key,
    make                 varchar(20)                                                       not null,
    model                varchar(20)                                                       not null,
    licence_plate_number varchar(20)                                                       not null,
    type                 enum ('SUV', 'Saloon', 'Hatchback', 'Cabriolet', 'Station Wagon') not null,
    color                varchar(20)                                                       not null,
    year_of_production   varchar(10)                                                       not null,
    owner_id             int                                                               not null,
    constraint vehicles_users_user_id_fk
        foreign key (owner_id) references users (id)
);

create table travels
(
    id              int auto_increment
        primary key,
    free_spots      smallint                     not null,
    departure_time  datetime                     not null,
    comment         varchar(300)                 null,
    driver_id       int                          null,
    vehicle_id      int                          null,
    status          enum ('ACTIVE', 'COMPLETED') not null,
    distance        double                       not null,
    departure_point varchar(100)                 not null,
    arrival_point   varchar(100)                 null,
    constraint travel_users_user_id_fk
        foreign key (driver_id) references users (id),
    constraint travel_vehicles_vehicle_id_fk
        foreign key (vehicle_id) references vehicles (id)
);

create table feedbacks
(
    id           int auto_increment
        primary key,
    creator_id   int      not null,
    travel_id    int      not null,
    comment      text     null,
    rating       smallint null,
    recipient_id int      null,
    constraint unique_user_id_travel_id
        unique (creator_id, travel_id),
    constraint feedback_travels_travel_id_fk
        foreign key (travel_id) references travels (id),
    constraint feedback_users_user_id_fk
        foreign key (creator_id) references users (id),
    constraint feedbacks_users_id_fk
        foreign key (recipient_id) references users (id)
);

create table travel_requests
(
    id        int auto_increment
        primary key,
    user_id   int                                      not null,
    travel_id int                                      not null,
    status    enum ('PENDING', 'APPROVED', 'REJECTED') not null,
    constraint travel_requests_travels_id_fk
        foreign key (travel_id) references travels (id),
    constraint travel_requests_users_id_fk
        foreign key (user_id) references users (id)
);

create table users_travels
(
    id        int auto_increment
        primary key,
    user_id   int not null,
    travel_id int not null,
    constraint fk_users_travels_travels
        foreign key (travel_id) references travels (id),
    constraint fk_users_travels_users
        foreign key (user_id) references users (id)
);

