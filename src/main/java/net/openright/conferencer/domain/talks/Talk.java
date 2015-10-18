package net.openright.conferencer.domain.talks;

import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

public class Talk {

    @Getter @Setter
    private Long eventId;

    @Getter @Setter
    private String title;

    public JSONObject toJSON() {
        return new JSONObject().put("title", title);
    }

}
