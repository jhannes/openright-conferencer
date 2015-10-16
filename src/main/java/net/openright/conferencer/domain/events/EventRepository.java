package net.openright.conferencer.domain.events;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.openright.conferencer.application.profile.UserProfile;

public interface EventRepository {

    long save(@Nonnull Event event);

    @Nonnull
    Collection<Event> list(@Nonnull UserProfile current);

}
