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

}
