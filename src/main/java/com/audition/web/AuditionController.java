package com.audition.web;

import static com.audition.common.exception.SystemException.BAD_REQUEST_ERROR_TITLE;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Audition API", description = "API endpoints for managing posts and comments")
public class AuditionController {

    private static final String POST_ID_VALIDATION_ERROR = "Post Id must contain only digits (0-9).";
    private static final String USER_ID_VALIDATION_ERROR = "UserId must contain only digits (0-9).";
    private static final String OK = "200";
    private static final String BAD_REQUEST = "400";
    private static final String NOT_FOUND = "404";
    @Autowired
    transient AuditionService auditionService;

    // TODO Add a query param that allows data filtering. The intent of the filter is at developers discretion.
    @Operation(summary = "Get all posts", description = "Retrieves all posts or filters posts by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = OK, description = "Successfully retrieved posts",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuditionPost.class)))
    })
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(
        @RequestParam(value = "userId", required = false) final String userId) {
        // TODO Add logic that filters response data based on the query param

        if (userId != null) {
            return auditionService.getPostsByUserId(validateAndParseId(userId, USER_ID_VALIDATION_ERROR));
        }
        return auditionService.getPosts();
    }

    @Operation(summary = "Get post by ID", description = "Retrieves a specific post by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = OK, description = "Successfully retrieved post",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuditionPost.class))),
        @ApiResponse(responseCode = BAD_REQUEST, description = "Invalid post ID format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = NOT_FOUND, description = "Post not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostsById(@PathVariable("id") final String id) {
        // TODO Add input validation
        // validateAndParseId is used for validations
        return auditionService.getPostById(validateAndParseId(id, POST_ID_VALIDATION_ERROR));
    }

    // TODO Add additional methods to return comments for each post. Hint: Check https://jsonplaceholder.typicode.com/
    @Operation(summary = "Get post with comments", description = "Retrieves a specific post along with all its comments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = OK, description = "Successfully retrieved post with comments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuditionPost.class))),
        @ApiResponse(responseCode = BAD_REQUEST, description = "Invalid post ID format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = NOT_FOUND, description = "Post not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @RequestMapping(value = "/posts/{id}/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPostWithCommentsByPostId(@PathVariable("id") final String id) {
        // TODO Add input validation
        // validateAndParseId is used for validations
        return auditionService.getPostWithCommentsByPostId(validateAndParseId(id, POST_ID_VALIDATION_ERROR));
    }

    @Operation(summary = "Get comments by post ID", description = "Retrieves all comments for a specific post")
    @ApiResponses(value = {
        @ApiResponse(responseCode = OK, description = "Successfully retrieved comments",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuditionComment.class))),
        @ApiResponse(responseCode = BAD_REQUEST, description = "Invalid post ID format",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = NOT_FOUND, description = "Post not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @RequestMapping(value = "/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionComment> getCommentsByPostId(
        @RequestParam(value = "postId") final String postId) {
        // TODO Add input validation
        // validateAndParseId is used for validations
        return auditionService.getCommentsByPostId(validateAndParseId(postId, POST_ID_VALIDATION_ERROR));
    }

    private Integer validateAndParseId(final String id, final String errorMessage) {
        if (id == null || id.trim().isEmpty()) {
            throw new SystemException(errorMessage, BAD_REQUEST_ERROR_TITLE, HttpStatus.BAD_REQUEST.value());
        }
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            throw new SystemException(errorMessage, BAD_REQUEST_ERROR_TITLE, HttpStatus.BAD_REQUEST.value());
        }
    }
}