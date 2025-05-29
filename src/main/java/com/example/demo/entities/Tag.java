package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Data
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "slug", unique = true, length = 50)
    private String slug;

    @ManyToMany(mappedBy = "tags")
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Post> posts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (slug == null && name != null) {
            slug = name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .trim();
        }
    }
}