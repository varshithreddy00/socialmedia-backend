package com.socialmedia.service;

import com.socialmedia.dto.request.UpdateProfileRequest;
import com.socialmedia.dto.response.UserResponse;

public interface UserService {
    UserResponse getUserById(Long userId);
    UserResponse getUserByUsername(String username, Long currentUserId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    void followUser(Long followerId, Long followeeId);
    void unfollowUser(Long followerId, Long followeeId);
    boolean isFollowing(Long followerId, Long followeeId);
}