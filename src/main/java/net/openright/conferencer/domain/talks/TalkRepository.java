package net.openright.conferencer.domain.talks;

import java.util.List;

import javax.annotation.Nonnull;

public interface TalkRepository {

    @Nonnull
    Long insert(@Nonnull Talk talk);

    @Nonnull
    List<Talk> list(@Nonnull Long eventId);

    @Nonnull
    Talk retrieve(@Nonnull Long id);

}
