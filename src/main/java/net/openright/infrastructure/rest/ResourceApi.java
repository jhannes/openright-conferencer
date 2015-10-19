package net.openright.infrastructure.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Tainted;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unused")
public interface ResourceApi {

    @Nonnull
    default Object createResource(@Nonnull JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    default Optional<JSONObject> getResource(@Nonnull @Tainted String id) {
        throw new UnsupportedOperationException();
    }

    default void updateResource(@Nonnull @Tainted String id, @Nonnull JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    default JSONObject listResources() {
        throw new UnsupportedOperationException();
    }

    default <T> List<T> convert(JSONArray jsonArray, Function<JSONObject, T> transformer) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            result.add(transformer.apply(jsonArray.getJSONObject(i)));
        }
        return result;
    }
}
