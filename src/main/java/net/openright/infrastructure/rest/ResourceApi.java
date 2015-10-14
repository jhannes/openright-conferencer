package net.openright.infrastructure.rest;

import javax.annotation.Nonnull;
import javax.annotation.Tainted;

import org.json.JSONObject;

public interface ResourceApi {

    @Nonnull
    default String createResource(@Nonnull JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    default JSONObject getResource(@Nonnull @Tainted String id) {
        throw new UnsupportedOperationException();
    }

    default void updateResource(@Nonnull @Tainted String id, JSONObject jsonObject) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    default JSONObject listResources() {
        throw new UnsupportedOperationException();
    }
}
