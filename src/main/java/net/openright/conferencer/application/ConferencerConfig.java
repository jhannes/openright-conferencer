package net.openright.conferencer.application;

import javax.annotation.Nonnull;

import net.openright.infrastructure.db.Database;

public interface ConferencerConfig {

    int getHttpPort();

    @Nonnull
    Database getDatabase();

    void start();

    @Nonnull
    String getGoogleClientId();

    @Nonnull
    String getGoogleClientSecret();
}
