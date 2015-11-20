create table Talk_comments (
	id serial primary key,
	talk_id integer not null references talks(id),
	title text not null,
	content text not null,
	author varchar(100) not null
);


