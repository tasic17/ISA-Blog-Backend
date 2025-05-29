package com.example.demo.mappers;

import com.example.demo.entities.Post;
import com.example.demo.entities.Tag;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class PostMapper {

    public static PostModel toModel(Post entity) {
        if (entity == null) return null;

        return PostModel.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .slug(entity.getSlug())
                .content(entity.getContent())
                .excerpt(entity.getExcerpt())
                .featuredImageUrl(entity.getFeaturedImageUrl())
                .authorId(entity.getAuthor().getId())
                .authorName(entity.getAuthor().getFullName())
                .authorEmail(entity.getAuthor().getEmail())
                .authorProfilePicture(entity.getAuthor().getProfilePictureUrl())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .status(entity.getStatus())
                .views(entity.getViews())
                .likesCount(entity.getLikesCount())
                .commentsCount(entity.getCommentsCount())
                .tags(entity.getTags().stream()
                        .map(TagMapper::toModel)
                        .collect(Collectors.toList()))
                .tagNames(entity.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .publishedAt(entity.getPublishedAt())
                .build();
    }

    public static PostModel toModelWithLikeStatus(Post entity, Integer currentUserId) {
        PostModel model = toModel(entity);
        if (model != null && currentUserId != null) {
            model.setLikedByCurrentUser(
                    entity.getLikedBy().stream()
                            .anyMatch(user -> user.getId().equals(currentUserId))
            );
        }
        return model;
    }

    public static List<PostModel> toModelList(List<Post> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .map(PostMapper::toModel)
                .collect(Collectors.toList());
    }

    public static PostPageModel toPageModel(Page<Post> page) {
        return PostPageModel.builder()
                .posts(toModelList(page.getContent()))
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}