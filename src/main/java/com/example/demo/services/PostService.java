package com.example.demo.services;

import com.example.demo.entities.Category;
import com.example.demo.entities.Post;
import com.example.demo.entities.Tag;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.mappers.PostMapper;
import com.example.demo.models.PostCreateModel;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import com.example.demo.repositories.ICategoryRepository;
import com.example.demo.repositories.IPostRepository;
import com.example.demo.repositories.ITagRepository;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final IPostRepository postRepository;
    private final IUserRepository userRepository;
    private final ICategoryRepository categoryRepository;
    private final ITagRepository tagRepository;

    public PostPageModel getPublishedPosts(int page, int size, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC,
                "publishedAt".equals(sortBy) ? "publishedAt" :
                        "views".equals(sortBy) ? "views" : "publishedAt");

        Page<Post> posts = postRepository.findByStatus(
                Post.PostStatus.PUBLISHED,
                PageRequest.of(page, size, sort)
        );

        return PostMapper.toPageModel(posts);
    }

    public PostPageModel getPostsByCategory(Integer categoryId, int page, int size) {
        Page<Post> posts = postRepository.findByCategoryIdAndStatus(
                categoryId,
                Post.PostStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );

        return PostMapper.toPageModel(posts);
    }

    public PostPageModel getPostsByTag(Integer tagId, int page, int size) {
        Page<Post> posts = postRepository.findByTagId(
                tagId,
                Post.PostStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );

        return PostMapper.toPageModel(posts);
    }

    public PostPageModel searchPosts(String search, int page, int size) {
        Page<Post> posts = postRepository.searchPosts(
                search,
                Post.PostStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"))
        );

        return PostMapper.toPageModel(posts);
    }

    @Transactional
    public PostModel getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        postRepository.incrementViews(post.getId());

        Integer currentUserId = getCurrentUserId();

        return PostMapper.toModelWithLikeStatus(post, currentUserId);
    }

    public PostModel getPostById(Integer id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Integer currentUserId = getCurrentUserId();
        return PostMapper.toModelWithLikeStatus(post, currentUserId);
    }

    @Transactional
    public PostModel createPost(PostCreateModel model) {
        User author = getCurrentUser();

        if (!author.isAuthor() && !author.isAdmin()) {
            throw new UnauthorizedException("User is not authorized to create posts");
        }

        Post post = new Post();
        post.setTitle(model.getTitle());
        post.setContent(model.getContent());
        post.setExcerpt(model.getExcerpt());
        post.setFeaturedImageUrl(model.getFeaturedImageUrl());
        post.setAuthor(author);

        if (model.getCategoryId() != null) {
            Category category = categoryRepository.findById(model.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            post.setCategory(category);
        }

        if (model.isPublish()) {
            post.setStatus(Post.PostStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
        } else {
            post.setStatus(Post.PostStatus.DRAFT);
        }

        post = postRepository.save(post);

        if (model.getTagNames() != null && !model.getTagNames().isEmpty()) {
            List<Tag> tags = new ArrayList<>();
            for (String tagName : model.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            post.setTags(tags);
            post = postRepository.save(post);
        }

        return PostMapper.toModel(post);
    }

    @Transactional
    public PostModel updatePost(Integer id, PostCreateModel model) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User currentUser = getCurrentUser();

        if (!currentUser.isAdmin() && !post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("User is not authorized to update this post");
        }

        post.setTitle(model.getTitle());
        post.setContent(model.getContent());
        post.setExcerpt(model.getExcerpt());
        post.setFeaturedImageUrl(model.getFeaturedImageUrl());

        if (model.getCategoryId() != null) {
            Category category = categoryRepository.findById(model.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            post.setCategory(category);
        }

        if (model.isPublish() && post.getStatus() != Post.PostStatus.PUBLISHED) {
            post.setStatus(Post.PostStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
        } else if (!model.isPublish()) {
            post.setStatus(Post.PostStatus.DRAFT);
        }

        post.getTags().clear();
        if (model.getTagNames() != null && !model.getTagNames().isEmpty()) {
            for (String tagName : model.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                post.addTag(tag);
            }
        }

        return PostMapper.toModel(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Integer id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User currentUser = getCurrentUser();

        if (!currentUser.isAdmin() && !post.getAuthor().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("User is not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public void toggleLike(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User currentUser = getCurrentUser();

        if (post.getLikedBy().contains(currentUser)) {
            post.getLikedBy().remove(currentUser);
        } else {
            post.getLikedBy().add(currentUser);
        }

        postRepository.save(post);
    }

    public PostPageModel getMyPosts(int page, int size, String status) {
        User currentUser = getCurrentUser();

        Page<Post> posts;
        if ("all".equals(status)) {
            posts = postRepository.findByAuthorId(
                    currentUser.getId(),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            );
        } else {
            Post.PostStatus postStatus = Post.PostStatus.valueOf(status.toUpperCase());
            posts = postRepository.findByAuthorIdAndStatus(
                    currentUser.getId(),
                    postStatus,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
            );
        }

        return PostMapper.toPageModel(posts);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Integer getCurrentUserId() {
        try {
            return getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }
}