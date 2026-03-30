package com.socialmedia.controller;

import com.socialmedia.dto.request.UpdateProfileRequest;
import com.socialmedia.dto.response.ApiResponse;
import com.socialmedia.dto.response.UserResponse;
import com.socialmedia.security.UserPrincipal;
import com.socialmedia.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User profile and follow operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UserResponse user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get user by username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        UserResponse user = userService.getUserByUsername(username, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse updated = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PostMapping("/{userId}/follow")
    @Operation(summary = "Follow a user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> followUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.followUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Followed successfully"));
    }

    @DeleteMapping("/{userId}/follow")
    @Operation(summary = "Unfollow a user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.unfollowUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Unfollowed successfully"));
    }

    @GetMapping("/{userId}/follow/status")
    @Operation(summary = "Check if current user follows another user",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Boolean>> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
        boolean following = userService.isFollowing(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(following));
    }
}