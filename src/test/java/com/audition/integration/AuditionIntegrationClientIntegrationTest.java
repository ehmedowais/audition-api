package com.audition.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;


@DisplayName("AuditionIntegrationClient Integration Tests")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditionIntegrationClientIntegrationTest {

    public static final String POSTS_URI = "/posts";
    public static final String COMMENTS_URI = "/comments";
    public static final String POSTS_URI_PATH = POSTS_URI + "/";
    public static final String CONTENT_TYPE = "Content-Type";
    private static WireMockServer wireMockServer;

    private transient AuditionIntegrationClient client;
    private transient ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", wireMockServer.port());
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();

        // Create ObjectMapper
        objectMapper = new ObjectMapper();

        // Create RestTemplate exactly like your production config
        RestTemplate restTemplate = new RestTemplate(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );
        restTemplate.setMessageConverters(List.of(
            new MappingJackson2HttpMessageConverter(objectMapper)
        ));

        // Create client with RestTemplate
        client = new AuditionIntegrationClient(restTemplate);
        ReflectionTestUtils.setField(client, "baseUrl", wireMockServer.baseUrl());
    }

    @Test
    @DisplayName("Should successfully fetch all posts from API")
    void shouldFetchAllPostsFromApi() throws Exception {
        // Given
        AuditionPost post1 = createPost(1, 1, "Post 1", "Body 1");
        AuditionPost post2 = createPost(2, 1, "Post 2", "Body 2");
        List<AuditionPost> posts = Arrays.asList(post1, post2);

        stubFor(get(urlEqualTo(POSTS_URI))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(posts))));

        // When
        List<AuditionPost> result = client.getPosts();

        // Then
        assertThat(result).hasSize(2).extracting(AuditionPost::getId).containsExactly(1, 2);
        verify(getRequestedFor(urlEqualTo(POSTS_URI)));
    }

    @Test
    @DisplayName("Should successfully fetch single post by ID")
    void shouldFetchPostByIdFromApi() throws Exception {
        // Given
        int postId = 1;
        AuditionPost post = createPost(postId, 1, "Test Post", "Test Body");

        stubFor(get(urlEqualTo(POSTS_URI_PATH + postId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(post))));

        // When
        AuditionPost result = client.getPostById(postId);

        // Then
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getTitle()).isEqualTo("Test Post");
        verify(getRequestedFor(urlEqualTo(POSTS_URI_PATH + postId)));
    }

    @Test
    @DisplayName("Should throw SystemException when post not found")
    void shouldThrowExceptionWhenPostNotFound() {
        // Given
        int postId = 999;
        stubFor(get(urlEqualTo(POSTS_URI_PATH + postId))
            .willReturn(aResponse().withStatus(404)));

        // When/Then
        assertThatThrownBy(() -> client.getPostById(postId))
            .isInstanceOf(SystemException.class)
            .satisfies(ex -> {
                SystemException sysEx = (SystemException) ex;
                assertThat(sysEx.getStatusCode()).isEqualTo(404);
            });
        verify(getRequestedFor(urlEqualTo(POSTS_URI_PATH + postId)));
    }

    @Test
    @DisplayName("Should throw SystemException on server error")
    void shouldThrowExceptionOnServerError() {
        // Given
        stubFor(get(urlEqualTo(POSTS_URI))
            .willReturn(aResponse().withStatus(500)));

        // When/Then
        assertThatThrownBy(() -> client.getPosts())
            .isInstanceOf(SystemException.class)
            .satisfies(ex -> {
                SystemException sysEx = (SystemException) ex;
                assertThat(sysEx.getStatusCode()).isEqualTo(500);
            });
    }

    @Test
    @DisplayName("Should successfully fetch posts by user ID")
    void shouldFetchPostsByUserIdFromApi() throws Exception {
        // Given
        int userId = 1;
        List<AuditionPost> posts = Arrays.asList(
            createPost(1, userId, "Post 1", "Body 1"),
            createPost(2, userId, "Post 2", "Body 2")
        );

        stubFor(get(urlPathEqualTo(POSTS_URI))
            .withQueryParam("userId", equalTo(String.valueOf(userId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(posts))));

        // When
        List<AuditionPost> result = client.getPostsByUserId(userId);

        // Then
        assertThat(result).hasSize(2).allMatch(p -> p.getUserId() == userId);
        verify(getRequestedFor(urlPathEqualTo(POSTS_URI)));
    }

    @Test
    @DisplayName("Should successfully fetch post with comments")
    void shouldFetchPostWithComments() throws Exception {
        // Given
        int postId = 1;
        AuditionPost post = createPost(postId, 1, "Test", "Body");
        List<AuditionComment> comments = Arrays.asList(
            createComment(1, postId, "Comment 1"),
            createComment(2, postId, "Comment 2")
        );

        stubFor(get(urlEqualTo(POSTS_URI_PATH + postId))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(post))));

        stubFor(get(urlEqualTo(POSTS_URI_PATH + postId + COMMENTS_URI))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(comments))));

        // When
        AuditionPost result = client.getPostWithCommentsByPostId(postId);

        // Then
        assertThat(result.getId()).isEqualTo(postId);
        assertThat(result.getComments()).hasSize(2);
    }

    @Test
    @DisplayName("Should successfully fetch comments by post ID")
    void shouldFetchCommentsByPostIdFromApi() throws Exception {
        // Given
        int postId = 1;
        List<AuditionComment> comments = Arrays.asList(
            createComment(1, postId, "Comment 1"),
            createComment(2, postId, "Comment 2"),
            createComment(3, postId, "Comment 3")
        );

        stubFor(get(urlPathEqualTo(COMMENTS_URI))
            .withQueryParam("postId", equalTo(String.valueOf(postId)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(objectMapper.writeValueAsString(comments))));

        // When
        List<AuditionComment> result = client.getCommentsByPostId(postId);

        // Then
        assertThat(result).hasSize(3).allMatch(c -> c.getPostId() == postId);
    }

    @Test
    @DisplayName("Should return empty list when no posts found")
    void shouldReturnEmptyList() throws Exception {
        // Given
        stubFor(get(urlEqualTo(POSTS_URI))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("[]")));

        // When
        List<AuditionPost> result = client.getPosts();

        // Then
        assertThat(result).isEmpty();
    }

    private AuditionPost createPost(int id, int userId, String title, String body) {
        AuditionPost post = new AuditionPost();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }

    private AuditionComment createComment(int id, int postId, String name) {
        AuditionComment comment = new AuditionComment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setName(name);
        comment.setEmail("test@example.com");
        comment.setBody("Test comment body");
        return comment;
    }
}