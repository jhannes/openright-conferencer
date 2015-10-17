create table Event_collaborators (
	id serial primary key,
	event_id integer not null references events(id),
	collaborator_email varchar(100) not null
);

