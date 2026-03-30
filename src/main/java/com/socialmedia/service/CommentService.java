package com.socialmedia.service;

import com.socialmedia.dto.request.CreateCommentRequest;
import com.socialmedia.dto.response.CommentResponse;
import com.socialmedia.dto.response.PagedResponse;

public interface CommentService {

    CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request);

    PagedResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size);

    void deleteComment(Long commentId, Long currentUserId);
}
