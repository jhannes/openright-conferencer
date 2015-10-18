package net.openright.conferencer.domain.talks;

import java.util.List;

public interface TalkRepository {

    Long insert(Talk talk);

    List<Talk> list(Long eventId);

}
