package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditionService {

    @Autowired
    private transient AuditionIntegrationClient auditionIntegrationClient;
    private static final Logger logger = LoggerFactory.getLogger(AuditionService.class);

    public List<AuditionPost> getPosts() {
        logger.info("Fetching all posts from the audition service");
        return auditionIntegrationClient.getPosts();
    }

    public List<AuditionPost> getPostsByUserId(Integer userId) {
        logger.info("Fetching all posts by user id {} from the audition service", userId);
        return auditionIntegrationClient.getPostsByUserId(userId);
    }

    public AuditionPost getPostById(final int postId) {
        logger.info("Fetching a specific post from the audition service by id {}", postId);
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPost getPostWithCommentsByPostId(final int postId) {
        logger.info("Fetching all comments for a specific post from the audition service by id {}", postId);
        return auditionIntegrationClient.getPostWithCommentsByPostId(postId);
    }

    public List<AuditionComment> getCommentsByPostId(final int postId) {
        logger.info("Fetching all comments for a specific post from the audition service ");
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }
}
