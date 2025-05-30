package com.example.demo.services;

import com.example.demo.entities.Category;
import com.example.demo.entities.Post;
import com.example.demo.entities.Tag;
import com.example.demo.entities.User;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UnauthorizedException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.mappers.PostMapper;
import com.example.demo.models.PostCreateModel;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import com.example.demo.repositories.ICategoryRepository;
import com.example.demo.repositories.IPostRepository;
import com.example.demo.repositories.ITagRepository;
import com.example.demo.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
@Slf4j
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

        // Proverava da li već postoji post sa sličnim naslovom
        String proposedSlug = generateSlugFromTitle(model.getTitle());
        if (postRepository.existsBySlug(proposedSlug)) {
            throw new ValidationException("Već postoji post sa sličnim naslovom. Molimo promenite naslov posta.");
        }

        Post post = new Post();
        post.setTitle(model.getTitle());
        post.setContent(model.getContent());
        post.setExcerpt(model.getExcerpt());
        post.setFeaturedImageUrl(model.getFeaturedImageUrl());
        post.setAuthor(author);

        // Postavi jedinstveni slug
        post.setSlug(generateUniqueSlug(model.getTitle()));

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

        try {
            post = postRepository.save(post);
            log.info("Successfully created post with ID: {} and slug: {}", post.getId(), post.getSlug());
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while creating post", e);
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("slug")) {
                throw new ValidationException("Već postoji post sa sličnim naslovom. Molimo promenite naslov posta.");
            }
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("title")) {
                throw new ValidationException("Već postoji post sa istim naslovom. Molimo promenite naslov posta.");
            }
            throw new ValidationException("Greška prilikom čuvanja posta. Molimo pokušajte ponovo.");
        } catch (Exception e) {
            log.error("Unexpected error while creating post", e);
            throw new ValidationException("Neočekivana greška prilikom kreiranja posta. Molimo pokušajte ponovo.");
        }

        // Dodaj tagove
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

        // Proveri slug samo ako se naslov menja
        if (!post.getTitle().equals(model.getTitle())) {
            String newSlug = generateSlugFromTitle(model.getTitle());
            if (!post.getSlug().equals(newSlug) && postRepository.existsBySlug(newSlug)) {
                throw new ValidationException("Već postoji post sa sličnim naslovom. Molimo promenite naslov posta.");
            }
            post.setSlug(generateUniqueSlug(model.getTitle()));
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

        try {
            return PostMapper.toModel(postRepository.save(post));
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while updating post", e);
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("slug")) {
                throw new ValidationException("Već postoji post sa sličnim naslovom. Molimo promenite naslov posta.");
            }
            throw new ValidationException("Greška prilikom ažuriranja posta. Molimo pokušajte ponovo.");
        }
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

    // HELPER METODE ZA SLUG GENERACIJU

    private String generateSlugFromTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "untitled-" + System.currentTimeMillis();
        }

        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .trim();
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = generateSlugFromTitle(title);

        if (baseSlug.isEmpty()) {
            baseSlug = "untitled";
        }

        String slug = baseSlug;
        int counter = 1;

        // Proveri da li slug već postoji i dodaj broj ako je potrebno
        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    // HELPER METODE ZA USER MANAGEMENT

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