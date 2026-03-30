package com.socialmedia.service.impl;

import com.socialmedia.dto.request.CreateCommentRequest;
import com.socialmedia.dto.response.CommentResponse;
import com.socialmedia.dto.response.PagedResponse;
import com.socialmedia.entity.Comment;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.exception.UnauthorizedException;
import com.socialmedia.repository.CommentRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long postId, Long authorId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Comment comment = new Comment(request.getContent(), post, author);
        Comment saved = commentRepository.save(comment);
        return mapToCommentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CommentResponse> getCommentsByPost(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        Page<CommentResponse> responsePage = comments.map(this::mapToCommentResponse);
        return PagedResponse.of(responsePage);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setPostId(comment.getPost().getId());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorUsername(comment.getAuthor().getUsername());
        response.setAuthorProfilePictureUrl(comment.getAuthor().getProfilePictureUrl());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
