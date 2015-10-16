package net.openright.conferencer.domain.events;

import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import net.openright.conferencer.application.profile.UserProfile;

public class Event {

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String creator;

    public JSONObject toJSON() {
        return new JSONObject().put("title", title);
    }

    public void setCreatorProfile(UserProfile userProfile) {
        creator = userProfile.getEmail();
    }

}
