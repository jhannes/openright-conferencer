package net.openright.conferencer.domain.talks;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import net.openright.infrastructure.db.Database;
import net.openright.lib.db.DatabaseTable;

public class DatabaseTalkRepository implements TalkRepository {

    private DatabaseTable table;

    public DatabaseTalkRepository(Database database) {
        this.table = new DatabaseTable(database, "talks");
    }

    @Override
    public Long insert(Talk talk) {
        return table.insert(row -> {
            row.put("event_id", talk.getEventId());
            row.put("title", talk.getTitle());
            row.put("created_at", Instant.now());
        });
    }

    @Override
    public List<Talk> list(Long eventId) {
        return table
                .where("event_id", eventId)
                .orderBy("created_at desc")
                .list(this::toTalk);
    }

    private Talk toTalk(Database.Row row) throws SQLException {
        Talk talk = new Talk();
        talk.setEventId(row.getLong("event_id"));
        talk.setTitle(row.getString("title"));
        return talk;
    }
}
