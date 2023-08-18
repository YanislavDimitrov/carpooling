alter table travels
add column is_deleted boolean not null;

update travels
set status = 'CANCELED'
where status = 'DELETED';
