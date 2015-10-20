create table Talks (
	id serial primary key,
	event_id integer not null references events(id),
	title varchar(100) not null,
	speaker_name varchar(100) null,
	speaker_email varchar(100) null,
	created_at timestamp not null
);

