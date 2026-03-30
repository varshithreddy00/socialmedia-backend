package com.socialmedia.controller;

import com.socialmedia.dto.request.CreateCommentRequest;
import com.socialmedia.dto.response.ApiResponse;
import com.socialmedia.dto.response.CommentResponse;
import com.socialmedia.dto.response.PagedResponse;
import com.socialmedia.security.UserPrincipal;
import com.socialmedia.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "Comments", description = "Add and delete comments on posts")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Add a comment to a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse comment = commentService.addComment(postId, currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }

    @GetMapping
    @Operation(summary = "Get all comments for a post (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<CommentResponse> comments =
                commentService.getCommentsByPost(postId, page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully"));
    }
}
