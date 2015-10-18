package net.openright.conferencer.domain.talks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.infrastructure.test.SampleData;

public class DatabaseTalkRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());
    private TalkRepository talkRepository = new DatabaseTalkRepository(config.getDatabase());
    private UserProfile userProfile = SampleData.sampleProfile();

    @Test
    public void shouldFindSavedTalks() throws Exception {
        Event event = SampleData.sampleEvent();
        try(AutoCloseable setAsCurrent = userProfile.setAsCurrent()) {
            eventRepository.insert(event);
        }

        Talk talk = SampleData.sampleTalk(event);
        try(AutoCloseable setAsCurrent = userProfile.setAsCurrent()) {
            talkRepository.insert(talk);
            assertThat(talkRepository.list(event.getId()))
                .extracting(Talk::getTitle)
                .contains(talk.getTitle());
        }
    }

}
