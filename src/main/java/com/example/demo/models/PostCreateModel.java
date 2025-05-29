package com.example.demo.models;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class PostCreateModel {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Excerpt must be less than 500 characters")
    private String excerpt;

    private String featuredImageUrl;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    private List<String> tagNames;

    private boolean publish;
}