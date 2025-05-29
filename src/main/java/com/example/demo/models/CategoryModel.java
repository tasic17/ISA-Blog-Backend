package com.example.demo.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryModel {
    private Integer id;
    private String name;
    private String description;
    private String slug;
    private Integer postCount;
}