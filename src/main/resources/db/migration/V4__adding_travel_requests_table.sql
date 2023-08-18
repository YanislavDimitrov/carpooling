
create table travel_requests
(
    id        int auto_increment primary key,
    user_id   int                                      not null,
    travel_id int                                      not null,
    status    enum ('PENDING', 'APPROVED', 'REJECTED') not null,
    constraint travel_requests_travels_id_fk
        foreign key (travel_id) references travels (id),
    constraint travel_requests_users_id_fk
        foreign key (user_id) references users (id)
);

ALTER table travels
    drop column start_latitude,
    drop column start_longitude,
    drop column end_latitude,
    drop column end_longitude,
    add column departure_point VARCHAR(100) NOT NULL,
    add column arrival_point   VARCHAR(100) NOT NULL;
