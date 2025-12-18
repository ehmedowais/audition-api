package com.audition.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditionIntegrationClient Tests")
class AuditionIntegrationClientTest {

    @Mock
    private transient RestTemplate restTemplate;

    private transient AuditionIntegrationClient client;

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String STATUS_CODE = "statusCode";
    private static final String POSTS_URI_PATH = "/posts/{id}";

    @BeforeEach
    void setUp() {
        client = new AuditionIntegrationClient(restTemplate);
        // Set the baseUrl using reflection (since it's @Value injected)
        ReflectionTestUtils.setField(client, "baseUrl", BASE_URL);
    }

    private AuditionPost createMockPost(int id) {
        AuditionPost post = new AuditionPost();
        post.setId(id);
        post.setUserId(1);
        post.setTitle("Test Post " + id);
        post.setBody("Test Body " + id);
        return post;
    }

    private AuditionComment createMockComment(int id, int postId) {
        AuditionComment comment = new AuditionComment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setName("Test Comment " + id);
        comment.setEmail("test" + id + "@example.com");
        comment.setBody("Test comment body " + id);
        return comment;
    }


    @Nested
    @DisplayName("getPosts() Tests")
    class GetPostsTests {

        @Test
        @DisplayName("Should successfully fetch all posts")
        void shouldFetchAllPosts() {
            // Given
            List<AuditionPost> expectedPosts = Arrays.asList(
                createMockPost(1),
                createMockPost(2),
                createMockPost(3)
            );

            ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok(expectedPosts);

            when(restTemplate.exchange(
                eq(BASE_URL + "/posts"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenReturn(response);

            // When
            List<AuditionPost> result = client.getPosts();

            // Then
            assertThat(result).isNotNull()
                .hasSize(3)
                .isEqualTo(expectedPosts);

            verify(restTemplate, times(1)).exchange(
                eq(BASE_URL + "/posts"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            );
        }

        @Test
        @DisplayName("Should return empty list when API returns empty list")
        void shouldReturnEmptyListWhenApiReturnsEmpty() {
            // Given
            ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok(List.of());

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenReturn(response);

            // When
            List<AuditionPost> result = client.getPosts();

            // Then
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should throw SystemException when response body is null")
        void shouldThrowExceptionWhenResponseBodyIsNull() {
            // Given
            ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok().build();

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenReturn(response);

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("No data found for posts");
        }

        @Test
        @DisplayName("Should throw SystemException on 404 Not Found")
        void shouldThrowExceptionOn404() {
            // Given
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Client error occurred")
                .extracting(STATUS_CODE).isEqualTo(404);
        }

        @Test
        @DisplayName("Should throw SystemException on 500 Server Error")
        void shouldThrowExceptionOn500() {
            // Given
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("External service error")
                .extracting(STATUS_CODE).isEqualTo(500);
        }

        @Test
        @DisplayName("Should throw SystemException on network timeout")
        void shouldThrowExceptionOnTimeout() {
            // Given
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(new ResourceAccessException("Connection timeout"));

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Unable to connect")
                .extracting(STATUS_CODE).isEqualTo(503);
        }
    }


    @Nested
    @DisplayName("getPostById() Tests")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should successfully fetch post by ID")
        void shouldFetchPostById() {
            // Given
            int postId = 1;
            AuditionPost expectedPost = createMockPost(postId);
            ResponseEntity<AuditionPost> response = ResponseEntity.ok(expectedPost);

            when(restTemplate.exchange(
                eq(BASE_URL + POSTS_URI_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("id", postId))
            )).thenReturn(response);

            // When
            AuditionPost result = client.getPostById(postId);

            // Then
            assertThat(result).isNotNull()
                .isEqualTo(expectedPost);
            assertThat(result.getId()).isEqualTo(postId);
        }

        @Test
        @DisplayName("Should throw SystemException when post not found")
        void shouldThrowExceptionWhenPostNotFound() {
            // Given
            int postId = 999;
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            // When/Then
            assertThatThrownBy(() -> client.getPostById(postId))
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("post with id " + postId);
        }


    }

    @Nested
    @DisplayName("getPostsByUserId() Tests")
    class GetPostsByUserIdTests {

        @Test
        @DisplayName("Should successfully fetch posts by user ID")
        void shouldFetchPostsByUserId() {
            // Given
            int userId = 1;
            List<AuditionPost> expectedPosts = Arrays.asList(
                createMockPost(1),
                createMockPost(2)
            );

            ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok(expectedPosts);

            when(restTemplate.exchange(
                eq(BASE_URL + "/posts?userId={userId}"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("userId", userId))
            )).thenReturn(response);

            // When
            List<AuditionPost> result = client.getPostsByUserId(userId);

            // Then
            assertThat(result).isNotNull()
                .hasSize(2)
                .isEqualTo(expectedPosts);
        }

        @Test
        @DisplayName("Should return empty list when user has no posts")
        void shouldReturnEmptyListWhenUserHasNoPosts() {
            // Given
            int userId = 999;
            ResponseEntity<List<AuditionPost>> response = ResponseEntity.ok(List.of());

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenReturn(response);

            // When
            List<AuditionPost> result = client.getPostsByUserId(userId);

            // Then
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("getPostWithCommentsByPostId() Tests")
    class GetPostWithCommentsByPostIdTests {

        @Test
        @DisplayName("Should successfully fetch post with comments")
        void shouldFetchPostWithComments() {
            // Given
            int postId = 1;
            AuditionPost expectedPost = createMockPost(postId);
            List<AuditionComment> expectedComments = Arrays.asList(
                createMockComment(1, postId),
                createMockComment(2, postId)
            );

            // Mock post fetch
            when(restTemplate.exchange(
                eq(BASE_URL + POSTS_URI_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("id", postId))
            )).thenReturn(ResponseEntity.ok(expectedPost));

            // Mock comments fetch
            when(restTemplate.exchange(
                eq(BASE_URL + "/posts/{postId}/comments"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("postId", postId))
            )).thenReturn(ResponseEntity.ok(expectedComments));

            // When
            AuditionPost result = client.getPostWithCommentsByPostId(postId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(postId);
            assertThat(result.getComments()).isNotNull()
                .hasSize(2)
                .isEqualTo(expectedComments);

            verify(restTemplate, times(2)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            );
        }

        @Test
        @DisplayName("Should throw exception when post not found")
        void shouldThrowExceptionWhenPostNotFound() {
            // Given

            when(restTemplate.exchange(
                contains(POSTS_URI_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

            // When/Then
            assertThatThrownBy(() -> client.getPostWithCommentsByPostId(999))
                .isInstanceOf(SystemException.class);
        }

        @Test
        @DisplayName("Should handle post without comments")
        void shouldHandlePostWithoutComments() {
            // Given
            int postId = 1;
            AuditionPost expectedPost = createMockPost(postId);

            when(restTemplate.exchange(
                eq(BASE_URL + POSTS_URI_PATH),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("id", postId))
            )).thenReturn(ResponseEntity.ok(expectedPost));

            when(restTemplate.exchange(
                eq(BASE_URL + "/posts/{postId}/comments"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("postId", postId))
            )).thenReturn(ResponseEntity.ok(List.of()));

            // When
            AuditionPost result = client.getPostWithCommentsByPostId(postId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getComments()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("getCommentsByPostId() Tests")
    class GetCommentsByPostIdTests {

        @Test
        @DisplayName("Should successfully fetch comments by post ID")
        void shouldFetchCommentsByPostId() {
            // Given
            int postId = 1;
            List<AuditionComment> expectedComments = Arrays.asList(
                createMockComment(1, postId),
                createMockComment(2, postId),
                createMockComment(3, postId)
            );

            ResponseEntity<List<AuditionComment>> response = ResponseEntity.ok(expectedComments);

            when(restTemplate.exchange(
                eq(BASE_URL + "/comments?postId={postId}"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                eq(Map.of("postId", postId))
            )).thenReturn(response);

            // When
            List<AuditionComment> result = client.getCommentsByPostId(postId);

            // Then
            assertThat(result).isNotNull()
                .hasSize(3)
                .isEqualTo(expectedComments);
        }

        @Test
        @DisplayName("Should return empty list when post has no comments")
        void shouldReturnEmptyListWhenNoComments() {
            // Given
            int postId = 999;
            ResponseEntity<List<AuditionComment>> response = ResponseEntity.ok(List.of());

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenReturn(response);

            // When
            List<AuditionComment> result = client.getCommentsByPostId(postId);

            // Then
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should throw SystemException on server error")
        void shouldThrowExceptionOnServerError() {
            // Given

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

            // When/Then
            assertThatThrownBy(() -> client.getCommentsByPostId(1))
                .isInstanceOf(SystemException.class)
                .extracting(STATUS_CODE).isEqualTo(502);
        }
    }


    @Nested
    @DisplayName("Error Handling Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should preserve original exception cause")
        void shouldPreserveExceptionCause() {
            // Given
            HttpClientErrorException originalException =
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(originalException);

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasCause(originalException);
        }

        @Test
        @DisplayName("Should handle different 4xx error codes")
        void shouldHandleDifferent4xxErrors() {
            // Test 401 Unauthorized
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .extracting(STATUS_CODE).isEqualTo(401);
        }

        @Test
        @DisplayName("Should handle different 5xx error codes")
        void shouldHandleDifferent5xxErrors() {
            // Test 503 Service Unavailable
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class),
                anyMap()
            )).thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

            assertThatThrownBy(() -> client.getPostById(1))
                .isInstanceOf(SystemException.class)
                .extracting(STATUS_CODE).isEqualTo(503);
        }

        @Test
        @DisplayName("Should handle runtime exceptions")
        void shouldHandleRuntimeExceptions() {
            // Given
            when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
            )).thenThrow(new RuntimeException("Unexpected error"));

            // When/Then
            assertThatThrownBy(() -> client.getPosts())
                .isInstanceOf(SystemException.class)
                .hasMessageContaining("Unexpected error occurred");
        }
    }
}
