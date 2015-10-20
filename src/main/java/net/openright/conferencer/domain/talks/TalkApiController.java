package net.openright.conferencer.domain.talks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.json.JSONArray;
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

    @Override
    public void updateResource(@Nonnull String id, @Nonnull JSONObject jsonObject) {
        Talk talk = repository.retrieve(Long.valueOf(id));
        talk.setTitle(jsonObject.getString("title"));
        talk.setSpeakerEmail(jsonObject.getJSONObject("speaker").getString("email"));
        talk.setSpeakerName(jsonObject.getJSONObject("speaker").getString("name"));
        repository.update(talk);
    }

    @Override
    @Nonnull
    public Optional<JSONObject> getResource(@Nonnull String id) {
        return Optional.of(
                repository.retrieve(Long.valueOf(id)).toJSON());
    }

    private Talk toTalk(JSONObject talkJSON) {
        Talk talk = new Talk();
        talk.setEventId(talkJSON.getLong("event_id"));
        talk.setTitle(talkJSON.getString("title"));
        talk.setTopicIds(convertLongArray(talkJSON.optJSONArray("topics")));
        return talk;
    }

    private List<Long> convertLongArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }
        ArrayList<Long> arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            arrayList.add(jsonArray.getLong(i));
        }
        return arrayList;
    }


}
