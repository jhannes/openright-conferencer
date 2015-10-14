package net.openright.conferencer.application;

import net.openright.infrastructure.db.Database;

public interface ConferencerConfig {

    int getHttpPort();

    Database getDatabase();

    void start();

    String getGoogleClientId();

    String getGoogleClientSecret();
}
