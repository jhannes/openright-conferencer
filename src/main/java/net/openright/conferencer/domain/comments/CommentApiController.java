package net.openright.conferencer.domain.comments;

import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.ResourceApi;

public class CommentApiController implements ResourceApi {

    private CommentRepository commentRepository;

    public CommentApiController(ConferencerConfig config) {
        commentRepository = new DatabaseCommentRepository(config.getDatabase());
    }

    @Override
    public Object createResource(JSONObject jsonObject) {
        return commentRepository.save(toComment(jsonObject));
    }

    private Comment toComment(JSONObject jsonObject) {
        Comment comment = new Comment();
        comment.setTitle(jsonObject.getString("title"));
        comment.setContent(jsonObject.getString("content"));
        comment.setTalkId(jsonObject.getLong("talk_id"));
        comment.setAuthor(UserProfile.getCurrent().getEmail());
        return comment;
    }

}
