package com.audition.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditionComment {

    private int id;
    private int postId;
    private String name;
    private String email;
    private String body;
}
