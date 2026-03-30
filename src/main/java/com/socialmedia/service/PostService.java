package com.socialmedia.service;

import com.socialmedia.dto.request.CreatePostRequest;
import com.socialmedia.dto.response.PagedResponse;
import com.socialmedia.dto.response.PostResponse;

public interface PostService {

    PostResponse createPost(Long authorId, CreatePostRequest request);

    PostResponse getPostById(Long postId, Long currentUserId);

    PostResponse updatePost(Long postId, Long currentUserId, CreatePostRequest request);

    void deletePost(Long postId, Long currentUserId);

    PagedResponse<PostResponse> getPostsByUser(String username, Long currentUserId, int page, int size);

    PagedResponse<PostResponse> getFeed(Long currentUserId, int page, int size);
}