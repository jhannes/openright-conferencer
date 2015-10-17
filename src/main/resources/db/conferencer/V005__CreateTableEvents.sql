create table Events (
	id serial primary key,
	slug varchar(30) not null unique,
	title text not null,
	creator varchar(100) not null,
	created_at timestamp,
	updated_at timestamp
);

