package net.openright.conferencer.domain.comments;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.conferencer.domain.talks.DatabaseTalkRepository;
import net.openright.conferencer.domain.talks.Talk;
import net.openright.conferencer.domain.talks.TalkComment;
import net.openright.conferencer.domain.talks.TalkRepository;
import net.openright.infrastructure.test.SampleData;

public class DatabaseCommentRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());
    private TalkRepository talkRepository = new DatabaseTalkRepository(config.getDatabase());
    private CommentRepository commentRepository = new DatabaseCommentRepository(config.getDatabase());
    private UserProfile userProfile = SampleData.sampleProfile();

    @Test
    public void shouldListCommentsForTalk() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);

        Talk talk = SampleData.sampleTalk(event);
        talkRepository.insert(talk);
        assertThat(talkRepository.list(event.getId()))
            .extracting(Talk::getTitle)
            .contains(talk.getTitle());

        Comment comment = SampleData.sampleComment(talk, userProfile);
        commentRepository.save(comment);

        assertThat(talkRepository.retrieve(talk.getId()).getComments())
            .extracting(TalkComment::getTitle)
            .contains(comment.getTitle());
    }


}
