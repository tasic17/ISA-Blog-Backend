package com.example.demo.mappers;

import com.example.demo.entities.Comment;
import com.example.demo.models.CommentModel;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {

    public static CommentModel toModel(Comment entity) {
        if (entity == null) return null;

        return CommentModel.builder()
                .id(entity.getId())
                .postId(entity.getPost().getId())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getFullName())
                .userProfilePicture(entity.getUser().getProfilePictureUrl())
                .parentCommentId(entity.getParentComment() != null ?
                        entity.getParentComment().getId() : null)
                .content(entity.getContent())
                .isApproved(entity.getIsApproved())
                .createdAt(entity.getCreatedAt())
                .replies(entity.getReplies() != null ?
                        toModelList(entity.getReplies()) : List.of())
                .build();
    }

    public static List<CommentModel> toModelList(List<Comment> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(CommentMapper::toModel)
                .collect(Collectors.toList());
    }
}