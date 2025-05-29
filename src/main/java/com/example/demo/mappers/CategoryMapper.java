package com.example.demo.mappers;

import com.example.demo.entities.Category;
import com.example.demo.models.CategoryModel;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static CategoryModel toModel(Category entity) {
        if (entity == null) return null;

        return CategoryModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .slug(entity.getSlug())
                .postCount(entity.getPosts() != null ? entity.getPosts().size() : 0)
                .build();
    }

    public static List<CategoryModel> toModelList(List<Category> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(CategoryMapper::toModel)
                .collect(Collectors.toList());
    }
}