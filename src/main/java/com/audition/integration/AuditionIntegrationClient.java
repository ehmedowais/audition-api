package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuditionIntegrationClient {

    private static final Logger logger = LoggerFactory.getLogger(AuditionIntegrationClient.class);
    private static final String SERVICE_NAME = "JSONPlaceholder API";
    private static final String NOT_FOUND_ERROR_TITLE = "Resource Not Found";
    private static final String DEFAULT_TITLE = "Internal Server Error";
    @SuppressFBWarnings(
        value = "EI2",
        justification = "RestTemplate is a Spring-managed, shared infrastructure bean"
    )
    private final transient RestTemplate restTemplate;

    @Value("${jsonplaceholder.api.url:https://jsonplaceholder.typicode.com}")
    private transient String baseUrl;

    public AuditionIntegrationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AuditionPost> getPosts() {
        String url = baseUrl + "/posts";
        logger.debug("Fetching all posts from {}", url);

        return executeRequest(
            url,
            HttpMethod.GET,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            },
            null,
            "posts"
        );
    }

    public AuditionPost getPostById(Integer postId) {
        String url = baseUrl + "/posts/{id}";
        logger.debug("Fetching post with id: {}", postId);

        return executeRequest(
            url,
            HttpMethod.GET,
            new ParameterizedTypeReference<AuditionPost>() {
            },
            Map.of("id", postId),
            "post with id " + postId
        );
    }

    public List<AuditionPost> getPostsByUserId(Integer userId) {
        String url = baseUrl + "/posts?userId={userId}";
        logger.debug("Fetching posts for user id: {}", userId);

        return executeRequest(
            url,
            HttpMethod.GET,
            new ParameterizedTypeReference<List<AuditionPost>>() {
            },
            Map.of("userId", userId),
            "posts for user id " + userId
        );
    }


    public AuditionPost getPostWithCommentsByPostId(Integer postId) {
        logger.debug("Fetching post with comments for post id: {}", postId);
        AuditionPost post = getPostById(postId);

        String url = baseUrl + "/posts/{postId}/comments";
        List<AuditionComment> comments = executeRequest(
            url,
            HttpMethod.GET,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            },
            Map.of("postId", postId),
            "comments for post id " + postId
        );

        post.setComments(comments);
        return post;
    }


    public List<AuditionComment> getCommentsByPostId(Integer postId) {
        String url = baseUrl + "/comments?postId={postId}";
        logger.debug("Fetching comments for post id: {}", postId);

        return executeRequest(
            url,
            HttpMethod.GET,
            new ParameterizedTypeReference<List<AuditionComment>>() {
            },
            Map.of("postId", postId),
            "comments for post id " + postId
        );
    }

    private <T> T executeRequest(
        String url,
        HttpMethod method,
        ParameterizedTypeReference<T> responseType,
        Map<String, ?> uriVariables,
        String resourceDescription) {

        try {
            ResponseEntity<T> response = uriVariables != null
                ? restTemplate.exchange(url, method, null, responseType, uriVariables)
                : restTemplate.exchange(url, method, null, responseType);

            if (response.getStatusCode().is2xxSuccessful()) {
                T body = response.getBody();
                if (body != null) {
                    logger.debug("Successfully fetched {}", resourceDescription);
                    return body;
                }

                // Handle null body with successful status (edge case)
                logger.warn("Received successful status but null body for {}", resourceDescription);
                throw new SystemException(
                    String.format("No data found for %s", resourceDescription),
                    NOT_FOUND_ERROR_TITLE,
                    HttpStatus.NOT_FOUND.value()
                );
            }

            // Handle non-2xx success codes
            logger.error("Unexpected status code {} for {}", response.getStatusCode(), resourceDescription);
            throw new SystemException(
                String.format("Unexpected response status for %s", resourceDescription),
                DEFAULT_TITLE,
                response.getStatusCode().value()
            );

        } catch (HttpClientErrorException e) {
            return handleClientError(e, resourceDescription);

        } catch (HttpServerErrorException e) {
            return handleServerError(e, resourceDescription);

        } catch (ResourceAccessException e) {
            return handleResourceAccessError(e, resourceDescription);

        } catch (SystemException e) {
            // Re-throw SystemException without wrapping
            throw e;

        } catch (Exception e) {
            return handleUnexpectedError(e, resourceDescription);
        }
    }

    private <T> T handleClientError(HttpClientErrorException e, String resourceDescription) {
        logger.error("Client error while fetching {}. Status: {}, Response: {}",
            resourceDescription, e.getStatusCode(), e.getResponseBodyAsString(), e);

        throw new SystemException(
            String.format("Client error occurred while fetching %s from %s: %s",
                resourceDescription, SERVICE_NAME, e.getMessage()),
            NOT_FOUND_ERROR_TITLE,
            e.getStatusCode().value(),
            e
        );
    }

    private <T> T handleServerError(HttpServerErrorException e, String resourceDescription) {
        logger.error("Server error from {} while fetching {}. Status: {}, Response: {}",
            SERVICE_NAME, resourceDescription, e.getStatusCode(), e.getResponseBodyAsString(), e);

        throw new SystemException(
            String.format("External service error occurred while fetching %s: %s",
                resourceDescription, e.getMessage()),
            String.format("%s Error", SERVICE_NAME),
            e.getStatusCode().value(),
            e
        );
    }

    private <T> T handleResourceAccessError(ResourceAccessException e, String resourceDescription) {
        logger.error("Network error while fetching {} from {}", resourceDescription, SERVICE_NAME, e);

        throw new SystemException(
            String.format("Unable to connect to %s while fetching %s: %s",
                SERVICE_NAME, resourceDescription, e.getMessage()),
            String.format("Backend service %s Unavailable", SERVICE_NAME),
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            e
        );
    }

    private <T> T handleUnexpectedError(Exception e, String resourceDescription) {
        logger.error("Unexpected error while fetching {}", resourceDescription, e);

        throw new SystemException(
            String.format("Unexpected error occurred while fetching %s: %s",
                resourceDescription, e.getMessage()),
            DEFAULT_TITLE,
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            e
        );
    }
}