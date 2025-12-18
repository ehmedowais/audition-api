package com.audition.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditionServiceTest {

    @Mock
    private transient AuditionIntegrationClient auditionIntegrationClient;

    @InjectMocks
    private transient AuditionService auditionService;

    @BeforeEach
    void setUp() {
        
    }

    @Test
    void getPosts_shouldReturnPostsFromIntegrationClient() {
        List<AuditionPost> posts = List.of(new AuditionPost());

        when(auditionIntegrationClient.getPosts()).thenReturn(posts);

        List<AuditionPost> result = auditionService.getPosts();

        assertSame(posts, result);
        verify(auditionIntegrationClient).getPosts();
    }

    @Test
    void getPostsByUserId_shouldReturnFilteredPosts() {
        Integer userId = 1;
        List<AuditionPost> posts = List.of(new AuditionPost());

        when(auditionIntegrationClient.getPostsByUserId(userId)).thenReturn(posts);

        List<AuditionPost> result = auditionService.getPostsByUserId(userId);

        assertSame(posts, result);
        verify(auditionIntegrationClient).getPostsByUserId(userId);
    }

    @Test
    void getPostById_shouldReturnPost() {
        int postId = 10;
        AuditionPost post = new AuditionPost();

        when(auditionIntegrationClient.getPostById(postId)).thenReturn(post);

        AuditionPost result = auditionService.getPostById(postId);

        assertSame(post, result);
        verify(auditionIntegrationClient).getPostById(postId);
    }

    @Test
    void getPostWithCommentsByPostId_shouldReturnPostWithComments() {
        int postId = 20;
        AuditionPost post = new AuditionPost();

        when(auditionIntegrationClient.getPostWithCommentsByPostId(postId)).thenReturn(post);

        AuditionPost result = auditionService.getPostWithCommentsByPostId(postId);

        assertSame(post, result);
        verify(auditionIntegrationClient).getPostWithCommentsByPostId(postId);
    }

    @Test
    void getCommentsByPostId_shouldReturnComments() {
        int postId = 30;
        List<AuditionComment> comments = List.of(new AuditionComment());

        when(auditionIntegrationClient.getCommentsByPostId(postId)).thenReturn(comments);

        List<AuditionComment> result = auditionService.getCommentsByPostId(postId);

        assertSame(comments, result);
        verify(auditionIntegrationClient).getCommentsByPostId(postId);
    }
}

