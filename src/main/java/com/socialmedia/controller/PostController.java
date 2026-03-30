package com.socialmedia.controller;

import com.socialmedia.dto.request.CreatePostRequest;
import com.socialmedia.dto.response.ApiResponse;
import com.socialmedia.dto.response.PagedResponse;
import com.socialmedia.dto.response.PostResponse;
import com.socialmedia.security.UserPrincipal;
import com.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@Tag(name = "Posts", description = "Create, read, update, delete posts")
public class PostController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "Create a new post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse post = postService.createPost(currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", post));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        PostResponse post = postService.getPostById(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update an existing post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreatePostRequest request) {
        PostResponse updated = postService.updatePost(postId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", updated));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        postService.deletePost(postId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get all posts by a user (paginated)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getPostsByUser(
            @PathVariable String username,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        PagedResponse<PostResponse> posts = postService.getPostsByUser(username, userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/feed")
    @Operation(summary = "Get the feed of posts from followed users (paginated)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PagedResponse<PostResponse>>> getFeed(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<PostResponse> feed = postService.getFeed(currentUser.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }
}
