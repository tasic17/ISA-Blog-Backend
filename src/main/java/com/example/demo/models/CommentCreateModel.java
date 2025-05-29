package com.example.demo.models;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class CommentCreateModel {
    @NotNull(message = "Post ID is required")
    private Integer postId;

    private Integer parentCommentId;

    @NotBlank(message = "Comment content is required")
    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String content;
}