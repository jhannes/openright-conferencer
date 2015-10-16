package net.openright.conferencer.application.profile;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.domain.events.DatabaseEventRepository;
import net.openright.conferencer.domain.events.EventRepository;
import net.openright.infrastructure.rest.JSONSource;

public class ProfileApiController implements JSONSource {

    private final EventRepository eventRepository;

    public ProfileApiController(ConferencerConfig config) {
        eventRepository = new DatabaseEventRepository(config.getDatabase());
    }

    @Override
    @Nonnull
    public JSONObject getJSON(HttpServletRequest req) {
        return new JSONObject()
                .put("username", UserProfile.getCurrent().getEmail())
                .put("events", new JSONArray(getEvents()));
    }

    private Collection<Object> getEvents() {
        return eventRepository.list(UserProfile.getCurrent()).stream()
                .map(e -> e.toJSON())
                .collect(Collectors.toList());
    }

}
