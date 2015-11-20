package net.openright.conferencer.domain.comments;

import lombok.Getter;
import lombok.Setter;

public class Comment {

    @Getter @Setter
    private Long id, talkId;

    @Getter @Setter
    private String title, content, author;

}
