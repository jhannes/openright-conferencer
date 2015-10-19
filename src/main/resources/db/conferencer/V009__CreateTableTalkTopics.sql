create table Talk_topics (
	id serial primary key,
	talk_id integer not null references talks(id),
	topic_id integer not null references event_topics(id),
	constraint talk_topics_constaint unique(talk_id, topic_id)
);


