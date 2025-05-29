package com.example.demo.repositories;

import com.example.demo.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ITagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);
    Optional<Tag> findBySlug(String slug);
    boolean existsByName(String name);
    List<Tag> findByNameIn(List<String> names);
}