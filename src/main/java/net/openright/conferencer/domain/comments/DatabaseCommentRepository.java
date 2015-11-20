package net.openright.conferencer.domain.comments;

import java.util.List;

import net.openright.conferencer.domain.talks.TalkComment;
import net.openright.infrastructure.db.Database;
import net.openright.lib.db.DatabaseTable;

public class DatabaseCommentRepository implements CommentRepository {

    private DatabaseTable table;

    public DatabaseCommentRepository(Database database) {
        table = new DatabaseTable(database, "talk_comments");
    }

    @Override
    public long save(Comment comment) {
        comment.setId(table.insert(row -> {
            row.put("talk_id", comment.getTalkId());
            row.put("title", comment.getTitle());
            row.put("content", comment.getContent());
            row.put("author", comment.getAuthor());
        }));
        return comment.getId();
    }

    public List<TalkComment> listTalkComments(Long talkId) {
        return table.where("talk_id", talkId).orderBy("id").list(row -> {
            TalkComment comment = new TalkComment();
            comment.setTitle(row.getString("title"));
            comment.setAuthor(row.getString("author"));
            return comment;
        });
    }

}
