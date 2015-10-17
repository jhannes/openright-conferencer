package net.openright.conferencer.domain.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
        JSONObject eventJSON = jsonObject.getJSONObject("event");
        eventRepository.doInTransaction(() -> {
            Event event = eventRepository.retrieve(id).orElseThrow(RequestException.notFound(id));
            JSONArray collaborators = eventJSON.getJSONArray("collaborators");
            for (int i = 0; i < collaborators.length(); i++) {
                event.addCollaborator(collaborators.getJSONObject(i).getString("email"));
            }
            event.setTopics(convert(eventJSON.getJSONArray("topics"), this::toEventTopic));
            eventRepository.update(event);
        });
    }

    private EventTopic toEventTopic(JSONObject o) {
        return new EventTopic(o.has("id") ? o.getLong("id") : null, o.optString("title"));
    }

    @Override
    @Nonnull
    public String createResource(@Nonnull JSONObject jsonObject) {
        return String.valueOf(eventRepository.insert(toEvent(jsonObject.getJSONObject("event"))));
    }

    private Event toEvent(JSONObject eventJSON) {
        Event event = new Event();
        event.setTitle(eventJSON.getString("title"));
        event.setSlug(eventJSON.getString("slug"));
        event.setCreatorProfile(UserProfile.getCurrent());
        return event;
    }

    private <T> List<T> convert(JSONArray jsonArray, Function<JSONObject, T> transformer) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            result.add(transformer.apply(jsonArray.getJSONObject(i)));
        }
        return result;
    }
}
