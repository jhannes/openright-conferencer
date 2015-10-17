package net.openright.conferencer.domain.events;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.rest.ResourceApi;

public class EventApiController implements ResourceApi {

    private EventRepository eventRepository;

    public EventApiController(ConferencerConfig config) {
        eventRepository = new DatabaseEventRepository(config.getDatabase());
    }

    @Override
    @Nonnull
    public Optional<JSONObject> getResource(@Nonnull String id) {
        return eventRepository.retrieve(id).map(Event::toJSON);
    }

    @Override
    public void updateResource(@Nonnull String id, @Nonnull JSONObject jsonObject) {
        eventRepository.doInTransaction(() -> {
            Event event = eventRepository.retrieve(id).orElseThrow(RequestException.notFound(id));
            JSONArray collaborators = jsonObject.getJSONObject("event").getJSONArray("collaborators");
            for (int i = 0; i < collaborators.length(); i++) {
                event.addCollaborator(collaborators.getJSONObject(i).getString("email"));
            }
            eventRepository.update(event);
        });
    }

    @Override
    @Nonnull
    public String createResource(@Nonnull JSONObject jsonObject) {
        return String.valueOf(eventRepository.insert(toEvent(jsonObject)));
    }

    private Event toEvent(JSONObject jsonObject) {
        JSONObject eventJSON = jsonObject.getJSONObject("event");
        Event event = new Event();
        event.setTitle(eventJSON.getString("title"));
        event.setSlug(eventJSON.getString("slug"));
        event.setCreatorProfile(UserProfile.getCurrent());
        return event;
    }

}
