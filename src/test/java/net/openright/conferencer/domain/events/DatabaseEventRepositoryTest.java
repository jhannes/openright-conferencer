package net.openright.conferencer.domain.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.Test;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.talks.DatabaseTalkRepository;
import net.openright.conferencer.domain.talks.Talk;
import net.openright.conferencer.domain.talks.TalkRepository;
import net.openright.infrastructure.test.SampleData;

public class DatabaseEventRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());
    private TalkRepository talkRepository = new DatabaseTalkRepository(config.getDatabase());
    private UserProfile userProfile = SampleData.sampleProfile();

    @Test
    public void shouldListMyEvents() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);
        assertThat(eventRepository.list(userProfile))
            .extracting(Event::getTitle)
            .contains(event.getTitle());
    }

    @Test
    public void shouldRetrieveEvent() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);

        eventRepository.insert(event);
        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get())
            .isEqualToIgnoringGivenFields(event, "id");
    }

    @Test
    public void shouldUpdateCollaborators() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);

        String collaborator = SampleData.randomEmail();
        event.addCollaborator(collaborator);

        eventRepository.update(event);

        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getCollaborators())
            .contains(collaborator);
    }

    @Test
    public void shouldInsertWithActiveUser() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);

        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getCreator())
            .isEqualTo(userProfile.getEmail());
    }

    @Test
    public void cantDeleteOwner() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        event.getCollaborators().remove(userProfile.getEmail());
        event.addCollaborator(SampleData.randomEmail());
        eventRepository.insert(event);

        event.getCollaborators().remove(userProfile.getEmail());

        eventRepository.update(event);

        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getCollaborators())
            .contains(userProfile.getEmail());
    }

    @Test
    public void shouldOnlyBeListableToCollaborators() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        UserProfile collaborator = SampleData.sampleProfile();
        event.addCollaborator(collaborator.getEmail());

        eventRepository.insert(event);
        assertThat(eventRepository.list(userProfile)).contains(event);

        assertThat(eventRepository.list(SampleData.sampleProfile())).doesNotContain(event);
    }

    @Test
    public void shouldOnlyBeRetrievableByCollaborators() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        UserProfile collaborator = SampleData.sampleProfile();
        event.addCollaborator(collaborator.getEmail());

        eventRepository.insert(event);
        assertThat(eventRepository.retrieve(event.getSlug(), userProfile)).isPresent();

        assertThat(eventRepository.retrieve(event.getSlug(), collaborator)).isPresent();

        assertThat(eventRepository.retrieve(event.getSlug(), SampleData.sampleProfile()))
            .isEmpty();
    }

    @Test
    public void shouldAddTopics() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);
        eventRepository.insert(event);

        event.setTopics(Arrays.asList(
                EventTopic.unsaved("some topic"), EventTopic.unsaved("other topic")));

        eventRepository.update(event);

        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getTopics())
            .extracting(EventTopic::getTitle)
            .containsExactly("some topic", "other topic");
    }

    @Test
    public void shouldUpdateTopics() throws Exception {
        Event event = SampleData.sampleEvent(userProfile);

        event.setTopics(Arrays.asList(
                EventTopic.unsaved("old 1"),
                EventTopic.unsaved("old 2"),
                EventTopic.unsaved("old 3")));
        eventRepository.insert(event);

        Event event2 = eventRepository.retrieve(event.getSlug(), userProfile).get();
        event2.setTopics(Arrays.asList(
                EventTopic.unsaved("new 1"),
                EventTopic.unsaved("new 2"),
                EventTopic.existing(event2.getTopics().get(1).getId()),
                EventTopic.existing(event2.getTopics().get(0).getId())));

        eventRepository.update(event2);
        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getTopics())
            .extracting(EventTopic::getTitle)
            .containsExactly("new 1", "new 2", "old 2", "old 1");
    }

    @Test
    public void shouldSummarizeTalksPerTopic() throws Exception {
        Event event = SampleData.sampleEventWithTopics(userProfile, 3);
        eventRepository.insert(event);
        event = eventRepository.retrieve(event.getSlug(), userProfile).get();

        for (int i=0; i<5; i++) {
            Talk talk = SampleData.sampleTalk(event);
            talk.setTopicIds(Arrays.asList(event.getTopics().get(0).getId()));
            talkRepository.insert(talk);
        }

        assertThat(eventRepository.retrieve(event.getSlug(), userProfile).get().getTopics())
            .extracting(EventTopic::getTalkCount)
            .containsExactly(5, 0, 0);
    }

}
