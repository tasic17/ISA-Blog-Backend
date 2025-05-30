package com.example.demo.repositories;

import com.example.demo.entities.Post;
import com.example.demo.entities.Post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorId(Integer authorId, Pageable pageable);

    Page<Post> findByAuthorIdAndStatus(Integer authorId, PostStatus status, Pageable pageable);

    Page<Post> findByCategoryId(Integer categoryId, Pageable pageable);

    Page<Post> findByCategoryIdAndStatus(Integer categoryId, PostStatus status, Pageable pageable);

    Optional<Post> findBySlug(String slug);

    // NOVE METODE ZA PROVERU POSTOJANJA
    boolean existsBySlug(String slug);

    boolean existsByTitle(String title);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> searchPosts(@Param("search") String search, @Param("status") PostStatus status, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = :status")
    Page<Post> findByTagId(@Param("tagId") Integer tagId, @Param("status") PostStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void incrementViews(@Param("postId") Integer postId);

    Page<Post> findByStatusOrderByViewsDesc(PostStatus status, Pageable pageable);

    Page<Post> findByStatusOrderByPublishedAtDesc(PostStatus status, Pageable pageable);

    // Admin functionality
    Long countByStatus(PostStatus status);

    @Query("SELECT c.name, COUNT(p) FROM Post p " +
            "RIGHT JOIN p.category c " +
            "WHERE p.status = 'PUBLISHED' OR p.status IS NULL " +
            "GROUP BY c.id, c.name " +
            "ORDER BY COUNT(p) DESC")
    List<Object[]> countPostsByCategory();

    @Query("SELECT p FROM Post p JOIN p.author a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    Page<Post> findByAuthorNameContaining(@Param("authorName") String authorName, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND " +
            "p.author.id IN (SELECT u.id FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :authorName, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :authorName, '%')))")
    Page<Post> findByStatusAndAuthorNameContaining(@Param("status") PostStatus status,
                                                   @Param("authorName") String authorName,
                                                   Pageable pageable);
}