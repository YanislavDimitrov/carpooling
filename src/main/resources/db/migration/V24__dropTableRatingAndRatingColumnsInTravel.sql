drop table travels_ratings;

alter table travels
    drop column average_rating,
    drop column total_ratings;

drop table users_travels;