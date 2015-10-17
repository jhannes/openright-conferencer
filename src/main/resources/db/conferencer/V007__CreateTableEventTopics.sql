create table Event_topics (
	id serial primary key,
	position integer not null,
	event_id integer not null references events(id),
	title varchar(100) not null
);

