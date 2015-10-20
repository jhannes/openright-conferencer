package net.openright.conferencer.domain.talks;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;

public class Talk {

    @Getter @Setter
    private Long id;

    @Getter @Setter
    private Long eventId;

    @Getter @Setter
    private String title, speakerEmail, speakerName;

    @Getter @Setter
    private List<Long> topicIds = new ArrayList<>();

    @Getter
    private List<TalkComment> comments = new ArrayList<>();

    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("title", title);
    }

}
