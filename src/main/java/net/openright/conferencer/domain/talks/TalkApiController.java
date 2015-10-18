package net.openright.conferencer.domain.talks;

import javax.annotation.Nonnull;

import org.json.JSONObject;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.infrastructure.rest.ResourceApi;

public class TalkApiController implements ResourceApi {

    private TalkRepository repository;

    public TalkApiController(ConferencerConfig config) {
        repository = new DatabaseTalkRepository(config.getDatabase());
    }

    @Override
    @Nonnull
    public Object createResource(@Nonnull JSONObject jsonObject) {
        return repository.insert(toTalk(jsonObject.getJSONObject("talk")));
    }

    private Talk toTalk(JSONObject talkJSON) {
        Talk talk = new Talk();
        talk.setEventId(talkJSON.getLong("event_id"));
        talk.setTitle(talkJSON.getString("title"));
        return talk;
    }


}
