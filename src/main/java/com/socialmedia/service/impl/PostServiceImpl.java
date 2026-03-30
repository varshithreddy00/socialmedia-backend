package com.socialmedia.service.impl;

import com.socialmedia.dto.request.CreatePostRequest;
import com.socialmedia.dto.response.PagedResponse;
import com.socialmedia.dto.response.PostResponse;
import com.socialmedia.entity.Follow;
import com.socialmedia.entity.Like;
import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import com.socialmedia.exception.ResourceNotFoundException;
import com.socialmedia.exception.UnauthorizedException;
import com.socialmedia.repository.FollowRepository;
import com.socialmedia.repository.LikeRepository;
import com.socialmedia.repository.PostRepository;
import com.socialmedia.repository.UserRepository;
import com.socialmedia.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository   postRepository;
    private final UserRepository   userRepository;
    private final LikeRepository   likeRepository;
    private final FollowRepository followRepository;

    public PostServiceImpl(PostRepository postRepository,
                           UserRepository userRepository,
                           LikeRepository likeRepository,
                           FollowRepository followRepository) {
        this.postRepository   = postRepository;
        this.userRepository   = userRepository;
        this.likeRepository   = likeRepository;
        this.followRepository = followRepository;
    }

    // ----------------------------------------------------------------
    // Create Post
    // ----------------------------------------------------------------
    @Override
    public PostResponse createPost(Long authorId, CreatePostRequest request) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authorId));

        Post post = new Post();
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setAuthor(author);

        Post saved = postRepository.save(post);
        return mapToResponse(saved, authorId);
    }

    // ----------------------------------------------------------------
    // Get Post by ID
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return mapToResponse(post, currentUserId);
    }

    // ----------------------------------------------------------------
    // Update Post
    // ----------------------------------------------------------------
    @Override
    public PostResponse updatePost(Long postId, Long currentUserId, CreatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to update this post");
        }

        post.setContent(request.getContent());
        if (request.getImageUrl() != null) {
            post.setImageUrl(request.getImageUrl());
        }

        Post updated = postRepository.save(post);
        return mapToResponse(updated, currentUserId);
    }

    // ----------------------------------------------------------------
    // Delete Post
    // ----------------------------------------------------------------
    @Override
    public void deletePost(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to delete this post");
        }

        postRepository.delete(post);
    }

    // ----------------------------------------------------------------
    // Get Posts by Username
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getPostsByUser(String username, Long currentUserId,
                                                       int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> posts = postRepository.findByAuthor(user, pageable);
        return buildPagedResponse(posts, currentUserId);
    }

    // ----------------------------------------------------------------
    // Get Feed
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostResponse> getFeed(Long currentUserId, int page, int size) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Get list of users that currentUser follows
        List<Follow> follows = followRepository.findByFollowerId(currentUserId);
        List<User> following = follows.stream()
                .map(Follow::getFollowee)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> posts;
        if (following.isEmpty()) {
            // Show own posts if not following anyone
            posts = postRepository.findByAuthor(currentUser, pageable);
        } else {
            following.add(currentUser);
            posts = postRepository.findByAuthorIn(following, pageable);
        }

        return buildPagedResponse(posts, currentUserId);
    }

    // ----------------------------------------------------------------
    // Helper: map Post → PostResponse
    // ----------------------------------------------------------------
    private PostResponse mapToResponse(Post post, Long currentUserId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());

        // Author info
        User author = post.getAuthor();
        response.setAuthorId(author.getId());
        response.setAuthorUsername(author.getUsername());
        response.setAuthorProfilePictureUrl(author.getProfilePictureUrl());

        // Counts
        response.setLikesCount(post.getLikes().size());
        response.setCommentsCount(post.getComments().size());

        // Like status for current user
        if (currentUserId != null) {
            boolean liked = post.getLikes().stream()
                    .map(Like::getUser)
                    .anyMatch(u -> u.getId().equals(currentUserId));
            response.setLikedByCurrentUser(liked);
        }

        return response;
    }

    // ----------------------------------------------------------------
    // Helper: build PagedResponse
    // ----------------------------------------------------------------
    private PagedResponse<PostResponse> buildPagedResponse(Page<Post> page, Long currentUserId) {
        List<PostResponse> content = page.getContent().stream()
                .map(p -> mapToResponse(p, currentUserId))
                .collect(Collectors.toList());

        PagedResponse<PostResponse> response = new PagedResponse<>();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }
}