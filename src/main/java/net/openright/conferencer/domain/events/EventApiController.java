package net.openright.conferencer.domain.events;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.talks.DatabaseTalkRepository;
import net.openright.conferencer.domain.talks.Talk;
import net.openright.conferencer.domain.talks.TalkRepository;
import net.openright.infrastructure.rest.RequestException;
import net.openright.infrastructure.rest.ResourceApi;

public class EventApiController implements ResourceApi {

    private EventRepository eventRepository;
    private TalkRepository talkRepository;

    public EventApiController(ConferencerConfig config) {
        eventRepository = new DatabaseEventRepository(config.getDatabase());
        talkRepository = new DatabaseTalkRepository(config.getDatabase());
    }

    @Override
    @Nonnull
    public Optional<JSONObject> getResource(@Nonnull String id) {
        return eventRepository.retrieve(id)
                .map(e -> new JSONObject()
                        .put("event", e.toJSON())
                        .put("talks", new JSONArray(getTalks(e))));
    }

    private Collection<Object> getTalks(Event e) {
        return talkRepository
                .list(e.getId())
                .stream().map(Talk::toJSON).collect(Collectors.toList());
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
            event.setTopics(convert(eventJSON.optJSONArray("topics"), this::toEventTopic));
            eventRepository.update(event);
        });
    }

    private EventTopic toEventTopic(JSONObject o) {
        return new EventTopic(o.has("id") ? o.getLong("id") : null, o.optString("title"));
    }

    @Override
    @Nonnull
    public Long createResource(@Nonnull JSONObject jsonObject) {
        return eventRepository.insert(toEvent(jsonObject.getJSONObject("event")));
    }

    private Event toEvent(JSONObject eventJSON) {
        Event event = new Event();
        event.setTitle(eventJSON.getString("title"));
        event.setSlug(eventJSON.getString("slug"));
        event.setCreatorProfile(UserProfile.getCurrent());
        return event;
    }
}
