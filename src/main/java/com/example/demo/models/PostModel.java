package com.example.demo.models;

import com.example.demo.entities.Post.PostStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostModel {
    private Integer id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String featuredImageUrl;

    private Integer authorId;
    private String authorName;
    private String authorEmail;
    private String authorProfilePicture;

    private Integer categoryId;
    private String categoryName;

    private PostStatus status;
    private Integer views;
    private Integer likesCount;
    private Integer commentsCount;

    private List<TagModel> tags;
    private List<String> tagNames;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private boolean isLikedByCurrentUser;
}