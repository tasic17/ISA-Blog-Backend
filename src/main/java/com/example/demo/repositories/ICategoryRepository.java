package com.example.demo.repositories;

import com.example.demo.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ICategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.posts WHERE c.id = :id")
    Optional<Category> findByIdWithPosts(Integer id);
}