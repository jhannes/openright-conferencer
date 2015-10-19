package net.openright.conferencer.domain.talks;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nonnull;

import net.openright.infrastructure.db.Database;
import net.openright.infrastructure.rest.RequestException;
import net.openright.lib.db.DatabaseTable;

public class DatabaseTalkRepository implements TalkRepository {

    private DatabaseTable table;
    private DatabaseTable talkTopicsTable;

    public DatabaseTalkRepository(Database database) {
        this.table = new DatabaseTable(database, "talks");
        this.talkTopicsTable = new DatabaseTable(database, "talk_topics");
    }

    @Override
    @Nonnull
    public Long insert(@Nonnull Talk talk) {
        talk.setId(table.insert(row -> {
            row.put("event_id", talk.getEventId());
            row.put("title", talk.getTitle());
            row.put("created_at", Instant.now());
        }));
        insertTalkTopics(talk);
        return talk.getId();
    }

    private void insertTalkTopics(Talk talk) {
        for (Long topicId : talk.getTopicIds()) {
            talkTopicsTable.insert(row -> {
                row.put("talk_id", talk.getId());
                row.put("topic_id", topicId);
            });
        }
    }

    @Override
    @Nonnull
    public Talk retrieve(@Nonnull Long id) {
        Talk talk = table.where("id", id).single(this::toTalk)
                .orElseThrow(RequestException.notFound(id));
        talk.setTopicIds(listTalkTopics(id));
        return talk;

    }

    private List<Long> listTalkTopics(Long id) {
        return talkTopicsTable.where("talk_id", id)
                .orderBy("topic_id")
                .list(row -> row.getLong("topic_id"));
    }

    @Override
    @Nonnull
    public List<Talk> list(@Nonnull Long eventId) {
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
