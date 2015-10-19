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
        try(AutoCloseable setAsCurrent = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();
            eventRepository.insert(event);
            assertThat(eventRepository.list())
                .extracting(Event::getTitle)
                .contains(event.getTitle());
        }
    }

    @Test
    public void shouldRetrieveEvent() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();

            eventRepository.insert(event);
            assertThat(eventRepository.retrieve(event.getSlug()).get())
                .isEqualToIgnoringGivenFields(event, "id");
        }
    }

    @Test
    public void shouldUpdateCollaborators() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();
            eventRepository.insert(event);

            String collaborator = SampleData.randomEmail();
            event.addCollaborator(collaborator);

            eventRepository.update(event);

            assertThat(eventRepository.retrieve(event.getSlug()).get().getCollaborators())
                .contains(collaborator);
        }
    }

    @Test
    public void shouldInsertWithActiveUser() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();
            event.setCreator(SampleData.randomEmail());
            eventRepository.insert(event);

            assertThat(eventRepository.retrieve(event.getSlug()).get().getCreator())
                .isEqualTo(userProfile.getEmail());
        }
    }

    @Test
    public void cantDeleteOwner() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();
            event.getCollaborators().remove(userProfile.getEmail());
            event.addCollaborator(SampleData.randomEmail());
            eventRepository.insert(event);

            event.getCollaborators().remove(userProfile.getEmail());

            eventRepository.update(event);

            assertThat(eventRepository.retrieve(event.getSlug()).get().getCollaborators())
                .contains(userProfile.getEmail());
        }

    }

    @Test
    public void shouldOnlyBeListableToCollaborators() throws Exception {
        Event event = SampleData.sampleEvent();
        UserProfile collaborator = SampleData.sampleProfile();
        event.addCollaborator(collaborator.getEmail());

        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            eventRepository.insert(event);
            assertThat(eventRepository.list()).contains(event);
        }
        try (AutoCloseable ignore = collaborator.setAsCurrent()) {
            assertThat(eventRepository.list()).contains(event);
        }
        try (AutoCloseable ignore = SampleData.sampleProfile().setAsCurrent()) {
            assertThat(eventRepository.list()).doesNotContain(event);
        }
    }

    @Test
    public void shouldOnlyBeRetrievableByCollaborators() throws Exception {
        Event event = SampleData.sampleEvent();
        UserProfile collaborator = SampleData.sampleProfile();
        event.addCollaborator(collaborator.getEmail());

        try(AutoCloseable ignore = userProfile.setAsCurrent()) {
            eventRepository.insert(event);
            assertThat(eventRepository.retrieve(event.getSlug())).isPresent();
        }
        try(AutoCloseable ignore = collaborator.setAsCurrent()) {
            assertThat(eventRepository.retrieve(event.getSlug())).isPresent();
        }
        try(AutoCloseable ignore = SampleData.sampleProfile().setAsCurrent()) {
            assertThat(eventRepository.retrieve(event.getSlug())).isEmpty();
        }
    }

    @Test
    public void shouldAddTopics() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();
            eventRepository.insert(event);

            event.setTopics(Arrays.asList(
                    EventTopic.unsaved("some topic"), EventTopic.unsaved("other topic")));

            eventRepository.update(event);

            assertThat(eventRepository.retrieve(event.getSlug()).get().getTopics())
                .extracting(EventTopic::getTitle)
                .containsExactly("some topic", "other topic");
        }
    }

    @Test
    public void shouldUpdateTopics() throws Exception {
        try (AutoCloseable ignore = userProfile.setAsCurrent()) {
            Event event = SampleData.sampleEvent();

            event.setTopics(Arrays.asList(
                    EventTopic.unsaved("old 1"),
                    EventTopic.unsaved("old 2"),
                    EventTopic.unsaved("old 3")));
            eventRepository.insert(event);

            Event event2 = eventRepository.retrieve(event.getSlug()).get();
            event2.setTopics(Arrays.asList(
                    EventTopic.unsaved("new 1"),
                    EventTopic.unsaved("new 2"),
                    EventTopic.existing(event2.getTopics().get(1).getId()),
                    EventTopic.existing(event2.getTopics().get(0).getId())));

            eventRepository.update(event2);
            assertThat(eventRepository.retrieve(event.getSlug()).get().getTopics())
                .extracting(EventTopic::getTitle)
                .containsExactly("new 1", "new 2", "old 2", "old 1");
        }
    }

    @Test
    public void shouldSummarizeTalksPerTopic() throws Exception {
        Event event = SampleData.sampleEventWithTopics(3);
        try(AutoCloseable setAsCurrent = userProfile.setAsCurrent()) {
            eventRepository.insert(event);
            event = eventRepository.retrieve(event.getSlug()).get();

            for (int i=0; i<5; i++) {
                Talk talk = SampleData.sampleTalk(event);
                talk.setTopicIds(Arrays.asList(event.getTopics().get(0).getId()));
                talkRepository.insert(talk);
            }

            assertThat(eventRepository.retrieve(event.getSlug()).get().getTopics())
                .extracting(EventTopic::getTalkCount)
                .containsExactly(5, 0, 0);
        }
    }



}
