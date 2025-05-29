package com.example.demo.mappers;

import com.example.demo.entities.Tag;
import com.example.demo.models.TagModel;

import java.util.List;
import java.util.stream.Collectors;

public class TagMapper {

    public static TagModel toModel(Tag entity) {
        if (entity == null) return null;

        return TagModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .postCount(entity.getPosts() != null ? entity.getPosts().size() : 0)
                .build();
    }

    public static List<TagModel> toModelList(List<Tag> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(TagMapper::toModel)
                .collect(Collectors.toList());
    }
}