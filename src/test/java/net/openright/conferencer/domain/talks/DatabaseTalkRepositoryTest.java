package net.openright.conferencer.domain.talks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.conferencer.domain.events.EventTopic;
import net.openright.infrastructure.test.SampleData;

public class DatabaseTalkRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());
    private TalkRepository talkRepository = new DatabaseTalkRepository(config.getDatabase());
    private UserProfile userProfile = SampleData.sampleProfile();

    @Test
    public void shouldFindSavedTalks() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);

        Talk talk = SampleData.sampleTalk(event);
        talkRepository.insert(talk);
        assertThat(talkRepository.list(event.getId()))
            .extracting(Talk::getTitle)
            .contains(talk.getTitle());
    }

    @Test
    public void shouldSaveTalkTopics() throws Exception {
        Event event = SampleData.sampleEventWithTopics(userProfile, 3);
        eventRepository.insert(event);
        event = eventRepository.retrieve(event.getSlug(), userProfile).get();

        Talk talk = SampleData.sampleTalk(event);
        List<EventTopic> topics = event.getTopics();
        talk.setTopicIds(Arrays.asList(
                topics.get(0).getId(),
                topics.get(2).getId()));
        talkRepository.insert(talk);
        assertThat(talkRepository.retrieve(talk.getId()).getTopicIds())
            .containsOnly(topics.get(0).getId(), topics.get(2).getId());
    }

    @Test
    public void shouldUpdateTalk() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);
        Talk talk = SampleData.sampleTalk(event);
        talkRepository.insert(talk);
        talk = talkRepository.retrieve(talk.getId());

        talk.setSpeakerEmail("some@email.com");
        talk.setTitle("new title");

        talkRepository.update(talk);
        assertThat(talkRepository.retrieve(talk.getId()))
            .isEqualToComparingFieldByField(talk);
    }

}
