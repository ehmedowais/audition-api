package com.audition.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.audition.AuditionApplication;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AuditionApplication.class)
@AutoConfigureMockMvc
class AuditionControllerTest {


    @Autowired
    private transient MockMvc mockMvc;
    @MockBean
    private transient AuditionService auditionService;

    @Autowired
    private transient ObjectMapper objectMapper;

    @Autowired
    AuditionLogger auditionLogger;

    @Test
    void getPosts_withoutUserId_shouldReturnAllPosts() throws Exception {
        List<AuditionPost> posts = List.of(new AuditionPost());

        when(auditionService.getPosts()).thenReturn(posts);

        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPosts_withValidUserId_shouldReturnFilteredPosts() throws Exception {
        List<AuditionPost> posts = List.of(new AuditionPost());

        when(auditionService.getPostsByUserId(1)).thenReturn(posts);

        mockMvc.perform(get("/posts")
                .param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPosts_withInvalidUserId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts")
                .param("userId", "abc"))
            .andExpect(status().isBadRequest());
    }


    @Test
    void getPostById_withValidId_shouldReturnPost() throws Exception {
        AuditionPost post = new AuditionPost();

        when(auditionService.getPostById(10)).thenReturn(post);

        mockMvc.perform(get("/posts/10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPostById_withInvalidId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/xyz"))
            .andExpect(status().isBadRequest());
    }


    @Test
    void getPostWithComments_withValidId_shouldReturnPost() throws Exception {
        AuditionPost post = new AuditionPost();

        when(auditionService.getPostWithCommentsByPostId(5)).thenReturn(post);

        mockMvc.perform(get("/posts/5/comments"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPostWithComments_withInvalidId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/posts/abc/comments"))
            .andExpect(status().isBadRequest());
    }


    @Test
    void getCommentsByPostId_withValidPostId_shouldReturnComments() throws Exception {
        List<AuditionComment> comments = List.of(new AuditionComment());

        when(auditionService.getCommentsByPostId(3)).thenReturn(comments);

        mockMvc.perform(get("/comments")
                .param("postId", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCommentsByPostId_withInvalidPostId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/comments")
                .param("postId", "xyz"))
            .andExpect(status().isBadRequest());
    }
}
