package net.openright.conferencer.domain.events;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.db.Database;
import net.openright.lib.db.DatabaseTable;

public class DatabaseEventRepository implements EventRepository {

    private final DatabaseTable eventsTable;
    private final DatabaseTable eventsCollaboratorTable;
    private Database database;
    private DatabaseTable eventTopicsTable;

    public DatabaseEventRepository(Database database) {
        this.database = database;
        this.eventsTable = new DatabaseTable(database, "events");
        this.eventTopicsTable = new DatabaseTable(database, "event_topics");
        this.eventsCollaboratorTable = new DatabaseTable(database, "event_collaborators");
    }

    @Override
    public void doInTransaction(@Nonnull Runnable operation) {
        database.doInTransaction(operation);
    }

    @Override
    @Nonnull
    public Optional<Event> retrieve(String slug) {
        Optional<Event> event = eventsTable
                .innerJoin(eventsCollaboratorTable, "event_id", "id")
                .where("slug", slug)
                .where("collaborator_email", UserProfile.getCurrent().getEmail())
                .single(this::toEvent);
        event.ifPresent(e -> e.getCollaborators().addAll(listCollaborators(e.getId())));
        event.ifPresent(e -> e.getTopics().addAll(listTopics(e.getId())));
        return event;
    }

    private Collection<? extends EventTopic> listTopics(Long id) {
        String query =
                "select id, title, "
                + "     (select count(*) from talk_topics where topic_id = t.id) as talk_count "
                + "from event_topics t where event_id = ? "
                + "order by position";
        return database.queryForList(query, Arrays.asList(id), row -> {
            EventTopic topic = new EventTopic(row.getLong("id"), row.getString("title"));
            topic.setTalkCount(row.getInt("talk_count"));
            return topic;
        });
    }

    @Override
    @Nonnull
    public Collection<Event> list() {
        return eventsTable
                .innerJoin(eventsCollaboratorTable, "event_id", "id")
                .where("collaborator_email", UserProfile.getCurrent().getEmail())
                .orderBy("title")
                .list(this::toEvent);
    }

    @Override
    public long insert(@Nonnull Event event) {
        event.setCreator(UserProfile.getCurrent().getEmail());
        long id = eventsTable.insert(row -> {
            row.put("slug", event.getSlug());
            row.put("title", event.getTitle());
            row.put("creator", event.getCreator());
        });
        event.setId(id);
        updateCollaborators(event);
        insertTopics(event.getId(), event.getTopics());
        return id;
    }

    @Override
    public void update(@Nonnull Event event) {
        updateCollaborators(event);
        updateTopics(event);
    }

    private void updateTopics(Event event) {
        deleteTopics(event.getId(), event.getTopics());
        updateTopicPositions(event.getTopics());
        insertTopics(event.getId(), event.getTopics());
    }

    private void updateTopicPositions(List<EventTopic> topics) {
        for (int i = 0; i < topics.size(); i++) {
            int position = i;
            EventTopic eventTopic = topics.get(i);
            if (eventTopic.getId() != null) {
                eventTopicsTable.where("id", eventTopic.getId())
                    .update(row -> row.put("position", position));
            }
        }
    }

    private void deleteTopics(Long id, List<EventTopic> topics) {
        Set<Long> preservedTopicIds = topics.stream()
                .map(EventTopic::getId)
                .filter(topicId -> topicId != null)
                .collect(Collectors.toSet());
        if (!preservedTopicIds.isEmpty()) {
            eventTopicsTable.where("event_id", id)
                .whereNotIn("id", preservedTopicIds)
                .delete();
        }
    }

    private void insertTopics(Long id, List<EventTopic> topics) {
        for (int i = 0; i < topics.size(); i++) {
            int position = i;
            EventTopic topic = topics.get(i);
            if (topic.getId() == null) {
                eventTopicsTable.insert(row -> {
                   row.put("event_id", id);
                   row.put("title", topic.getTitle());
                   row.put("position", position);
                });
            }
        }
    }

    @Nonnull
    private Collection<? extends String> listCollaborators(Long id) {
        return eventsCollaboratorTable.where("event_id", id)
                .orderBy("collaborator_email")
                .list(row -> row.getString("collaborator_email"));
    }

    private void updateCollaborators(Event event) {
        event.addCollaborator(event.getCreator());
        deleteCollaborators(event.getId());
        insertCollaborators(event.getId(), event.getCollaborators());
    }

    private void deleteCollaborators(Long id) {
        eventsCollaboratorTable.where("event_id", id).delete();
    }

    private void insertCollaborators(Long id, Collection<String> collaborators) {
        for (String email : collaborators) {
            eventsCollaboratorTable.insert(row -> {
               row.put("event_id", id);
               row.put("collaborator_email", email);
            });
        }
    }

    private Event toEvent(Database.Row row) throws SQLException {
        Event event = new Event();
        event.setId(row.getLong("id"));
        event.setSlug(row.getString("slug"));
        event.setTitle(row.getString("title"));
        event.setCreator(row.getString("creator"));
        return event;
    }
}
