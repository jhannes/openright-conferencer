package net.openright.conferencer.domain.events;

import java.util.Collection;
import java.util.HashSet;

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

    @Getter(AccessLevel.PROTECTED)
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

    public JSONObject toJSON() {
        return new JSONObject()
                .put("title", title)
                .put("slug", slug);
    }

    public void setCreatorProfile(UserProfile userProfile) {
        creator = userProfile.getEmail();
        collaborators.add(userProfile.getEmail());
    }

    public void addCollaborator(String email) {
        collaborators.add(email);
    }

}
