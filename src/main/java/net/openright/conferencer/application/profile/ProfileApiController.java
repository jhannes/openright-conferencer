package net.openright.conferencer.application.profile;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import net.openright.infrastructure.rest.JSONSource;

public class ProfileApiController implements JSONSource {

    @Override
    @Nonnull
    public JSONObject getJSON(HttpServletRequest req) {
        return new JSONObject()
                .put("username", UserProfile.getCurrent().getEmail())
                .put("events", new JSONArray());
    }

}
