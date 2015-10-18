package net.openright.conferencer.domain.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.openright.conferencer.application.profile.UserProfile;

@ToString
@EqualsAndHashCode(of={"id", "slug", "title"})
public class Event {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String slug;

    @Getter @Setter
    private String creator;

    @Getter
    private Collection<String> collaborators = new HashSet<>();

    @Getter @Setter
    private List<EventTopic> topics = new ArrayList<>();

    public JSONObject toJSON() {
        return new JSONObject()
                .put("id", id)
                .put("title", title)
                .put("slug", slug)
                .put("topics", new JSONArray(getTopicsJSON()));
    }

    private Collection<Object> getTopicsJSON() {
        return topics.stream().map(EventTopic::toJSON).collect(Collectors.toList());
    }

    public void setCreatorProfile(UserProfile userProfile) {
        creator = userProfile.getEmail();
        collaborators.add(userProfile.getEmail());
    }

    public void addCollaborator(String email) {
        collaborators.add(email);
    }

    public void addTopic(String topicTitle) {
        this.topics.add(new EventTopic(null, topicTitle));
    }

    public void preserveTopic(Long id) {
        this.topics.add(new EventTopic(id, null));
    }

}
