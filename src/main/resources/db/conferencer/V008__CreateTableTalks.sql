create table Talks (
	id serial primary key,
	event_id integer not null references events(id),
	title varchar(100) not null,
	created_at timestamp not null
);

