package net.openright.conferencer.domain.events;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

import net.openright.conferencer.application.profile.UserProfile;

public interface EventRepository {

    long insert(@Nonnull Event event);

    @Nonnull
    Collection<Event> list();

    @Nonnull
    Collection<Event> list(UserProfile userProfile);

    @Nonnull
    Optional<Event> retrieve(String slug);

    @Nonnull
    Optional<Event> retrieve(String slug, UserProfile userProfile);

    void update(@Nonnull Event event);

    void doInTransaction(@Nonnull Runnable object);

}
