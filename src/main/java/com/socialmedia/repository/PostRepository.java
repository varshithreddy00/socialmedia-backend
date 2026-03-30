package com.socialmedia.repository;

import com.socialmedia.entity.Post;
import com.socialmedia.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByAuthor(User author, Pageable pageable);

    Page<Post> findByAuthorIn(List<User> authors, Pageable pageable);
}