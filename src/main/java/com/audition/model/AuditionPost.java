package com.audition.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditionPost {

    private int userId;
    private int id;
    private String title;
    private String body;
    private List<AuditionComment> comments;

    public List<AuditionComment> getComments() {
        return comments == null
            ? List.of()
            : Collections.unmodifiableList(comments);
    }

    public void setComments(List<AuditionComment> comments) {
        this.comments = comments == null
            ? new ArrayList<>()
            : new ArrayList<>(comments);
    }

}
