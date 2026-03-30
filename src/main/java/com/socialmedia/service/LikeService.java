package com.socialmedia.service;

public interface LikeService {

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    long getLikeCount(Long postId);

    boolean hasUserLikedPost(Long postId, Long userId);
}
