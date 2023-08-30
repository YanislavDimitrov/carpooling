alter table travels
    drop column price ;

alter table travels
add column price varchar(20) not null
