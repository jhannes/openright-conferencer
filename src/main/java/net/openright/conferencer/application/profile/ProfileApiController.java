package net.openright.conferencer.application.profile;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.infrastructure.rest.ResourceApi;

public class ProfileApiController implements ResourceApi {

    public ProfileApiController(ConferencerConfig config) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public JSONObject listResources() {
        return new JSONObject()
                .put("username", UserProfile.getCurrent().getEmail())
                .put("events", new JSONArray());
    }

}
