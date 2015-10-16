package net.openright.conferencer.domain.events;

import java.sql.SQLException;
import java.util.Collection;

import javax.annotation.Nonnull;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.db.Database;
import net.openright.lib.db.DatabaseTable;

public class DatabaseEventRepository implements EventRepository {

    private final DatabaseTable table;

    public DatabaseEventRepository(Database database) {
        this.table = new DatabaseTable(database, "events");
    }

    @Override
    @Nonnull
    public Collection<Event> list(@Nonnull UserProfile current) {
        return table
                .where("creator", current.getEmail())
                .list(this::toEvent);
    }

    @Override
    public long save(@Nonnull Event event) {
        return table.insert(row -> {
            row.put("creator", event.getCreator());
            row.put("title", event.getTitle());
        });
    }

    private Event toEvent(Database.Row row) throws SQLException {
        Event event = new Event();
        event.setTitle(row.getString("title"));
        return event;
    }

}
