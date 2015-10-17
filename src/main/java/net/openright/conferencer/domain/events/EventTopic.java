package net.openright.conferencer.domain.events;

import org.json.JSONObject;

import lombok.Getter;

public class EventTopic {

    @Getter
    private Long id;

    @Getter
    private String title;

    public EventTopic(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public static EventTopic unsaved(String title) {
        return new EventTopic(null, title);
    }

    public static EventTopic existing(Long id) {
        return new EventTopic(id, null);
    }

    public JSONObject toJSON() {
        return new JSONObject().put("id", id).put("title", title);
    }

}
