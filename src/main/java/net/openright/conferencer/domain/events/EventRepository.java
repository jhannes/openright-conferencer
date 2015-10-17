package net.openright.conferencer.domain.events;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;

public interface EventRepository {

    long insert(@Nonnull Event event);

    @Nonnull
    Collection<Event> list();

    @Nonnull
    Optional<Event> retrieve(String slug);

    void update(@Nonnull Event event);

    void doInTransaction(@Nonnull Runnable object);

}
