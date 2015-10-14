package net.openright.conferencer.application;

import javax.annotation.Nonnull;

import net.openright.infrastructure.db.Database;

public interface ConferencerConfig {

    int getHttpPort();

    @Nonnull
    Database getDatabase();

    @Nonnull
    String getGoogleClientId();

    @Nonnull
    String getGoogleClientSecret();
}
