package net.openright.conferencer.domain.events;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.db.Database;
import net.openright.lib.db.DatabaseTable;

public class DatabaseEventRepository implements EventRepository {

    private final DatabaseTable eventsTable;
    private final DatabaseTable eventsCollaboratorTable;
    private Database database;

    public DatabaseEventRepository(Database database) {
        this.database = database;
        this.eventsTable = new DatabaseTable(database, "events");
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
        return event;
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
        return id;
    }

    @Override
    public void update(@Nonnull Event event) {
        updateCollaborators(event);
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
