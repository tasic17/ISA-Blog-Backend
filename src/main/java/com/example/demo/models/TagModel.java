package com.example.demo.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TagModel {
    private Integer id;
    private String name;
    private String slug;
    private Integer postCount;
}