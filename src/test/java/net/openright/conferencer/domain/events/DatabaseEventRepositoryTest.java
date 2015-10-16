package net.openright.conferencer.domain.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.test.SampleData;

public class DatabaseEventRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private EventRepository eventRepository = new DatabaseEventRepository(config.getDatabase());

    @Test
    public void shouldListMyEvents() throws Exception {
        UserProfile userProfile = SampleData.sampleProfile();

        Event event = new Event();
        event.setTitle(SampleData.sampleString(4));
        event.setCreatorProfile(userProfile);
        eventRepository.save(event);

        assertThat(eventRepository.list(userProfile))
            .extracting(Event::getTitle)
            .contains(event.getTitle());
    }

}
