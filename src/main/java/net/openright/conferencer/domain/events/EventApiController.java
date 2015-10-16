package net.openright.conferencer.domain.events;

import javax.annotation.Nonnull;

import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.ResourceApi;

public class EventApiController implements ResourceApi {

    private EventRepository eventRepository;

    public EventApiController(ConferencerConfig config) {
        eventRepository = new DatabaseEventRepository(config.getDatabase());
    }

    @Override
    @Nonnull
    public String createResource(@Nonnull JSONObject jsonObject) {
        return String.valueOf(eventRepository.save(toEvent(jsonObject)));
    }

    private Event toEvent(JSONObject jsonObject) {
        Event event = new Event();
        event.setTitle(jsonObject.getJSONObject("event").getString("title"));
        event.setCreatorProfile(UserProfile.getCurrent());
        return event;
    }

}
