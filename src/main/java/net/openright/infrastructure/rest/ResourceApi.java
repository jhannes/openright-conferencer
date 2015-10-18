package net.openright.infrastructure.rest;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Tainted;

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
}
