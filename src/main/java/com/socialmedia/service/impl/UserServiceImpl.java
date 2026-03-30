package com.socialmedia.service.impl;

import com.socialmedia.dto.request.UpdateProfileRequest;
import com.socialmedia.dto.response.UserResponse;
import com.socialmedia.entity.Follow;
import com.socialmedia.entity.User;
import com.socialmedia.exception.BadRequestException;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.repository.FollowRepository;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository   userRepository;
    private final FollowRepository followRepository;

    public UserServiceImpl(UserRepository userRepository,
                           FollowRepository followRepository) {
        this.userRepository   = userRepository;
        this.followRepository = followRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToResponse(user, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToResponse(user, currentUserId);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        User updated = userRepository.save(user);
        return mapToResponse(updated, null);
    }

    @Override
    public void followUser(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new BadRequestException("You are already following this user");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerId));
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followeeId));

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowee(followee);
        followRepository.save(follow);
    }

    @Override
    public void unfollowUser(Long followerId, Long followeeId) {
        Follow follow = followRepository
                .findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new BadRequestException("You are not following this user"));

        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    private UserResponse mapToResponse(User user, Long currentUserId) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setCreatedAt(user.getCreatedAt());

        response.setFollowersCount(followRepository.countByFolloweeId(user.getId()));
        response.setFollowingCount(followRepository.countByFollowerId(user.getId()));

        response.setPostsCount(user.getPosts() != null ? user.getPosts().size() : 0);

        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            response.setFollowedByCurrentUser(
                followRepository.existsByFollowerIdAndFolloweeId(currentUserId, user.getId())
            );
        }

        return response;
    }
}