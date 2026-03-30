package com.socialmedia.controller;

import com.socialmedia.dto.response.ApiResponse;
import com.socialmedia.security.UserPrincipal;
import com.socialmedia.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/likes")
@Tag(name = "Likes", description = "Like and unlike posts")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    @Operation(summary = "Like a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        likeService.likePost(postId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully"));
    }

    @DeleteMapping
    @Operation(summary = "Unlike a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        likeService.unlikePost(postId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully"));
    }

    @GetMapping("/count")
    @Operation(summary = "Get the like count for a post")
    public ResponseEntity<ApiResponse<Long>> getLikeCount(
            @PathVariable Long postId) {
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/status")
    @Operation(summary = "Check if the current user has liked a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Boolean>> getLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean liked = likeService.hasUserLikedPost(postId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(liked));
    }
}
