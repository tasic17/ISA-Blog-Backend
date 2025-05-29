package com.example.demo.repositories;

import com.example.demo.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ICommentRepository extends JpaRepository<Comment, Integer> {

    Page<Comment> findByPostIdAndParentCommentIsNull(Integer postId, Pageable pageable);

    List<Comment> findByPostId(Integer postId);

    Page<Comment> findByUserId(Integer userId, Pageable pageable);

    Page<Comment> findByIsApprovedFalse(Pageable pageable);

    Long countByPostId(Integer postId);

    Long countByIsApprovedFalse();
}