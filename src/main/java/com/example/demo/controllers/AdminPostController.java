package com.example.demo.controllers;

import com.example.demo.entities.Post;
import com.example.demo.mappers.PostMapper;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import com.example.demo.repositories.IPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminPostController {

    private final IPostRepository postRepository;

    @GetMapping
    public ResponseEntity<PostPageModel> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String authorName
    ) {
        log.info("Admin fetching all posts - page: {}, size: {}", page, size);

        Sort sort = sortDir.equals("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Page<Post> postPage;

        try {
            if (status != null) {
                Post.PostStatus postStatus = Post.PostStatus.valueOf(status.toUpperCase());
                postPage = postRepository.findByStatus(postStatus, PageRequest.of(page, size, sort));
            } else {
                postPage = postRepository.findAll(PageRequest.of(page, size, sort));
            }

            PostPageModel result = PostMapper.toPageModel(postPage);
            log.info("Successfully fetched {} posts", result.getPosts().size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching posts", e);
            throw new RuntimeException("Error fetching posts: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostModel> getPostById(@PathVariable Integer id) {
        log.info("Admin fetching post by id: {}", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(PostMapper.toModel(post));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PostModel> updatePostStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
        log.info("Admin updating post {} status to {}", id, status);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Post.PostStatus newStatus = Post.PostStatus.valueOf(status.toUpperCase());
        post.setStatus(newStatus);

        if (newStatus == Post.PostStatus.PUBLISHED && post.getPublishedAt() == null) {
            post.setPublishedAt(java.time.LocalDateTime.now());
        }

        postRepository.save(post);
        return ResponseEntity.ok(PostMapper.toModel(post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        log.info("Admin deleting post: {}", id);
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPostStats() {
        log.info("Admin fetching post stats");

        try {
            long totalPosts = postRepository.count();
            long publishedPosts = postRepository.countByStatus(Post.PostStatus.PUBLISHED);
            long draftPosts = postRepository.countByStatus(Post.PostStatus.DRAFT);
            long archivedPosts = postRepository.countByStatus(Post.PostStatus.ARCHIVED);

            // Safely fetch category stats
            List<Object[]> categoryStatsRaw;
            try {
                categoryStatsRaw = postRepository.countPostsByCategory();
            } catch (Exception e) {
                log.warn("Error fetching category stats, using empty list", e);
                categoryStatsRaw = List.of();
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPosts", totalPosts);
            stats.put("publishedPosts", publishedPosts);
            stats.put("draftPosts", draftPosts);
            stats.put("archivedPosts", archivedPosts);
            stats.put("categoryStats", categoryStatsRaw);

            log.info("Successfully fetched stats: {}", stats);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching post stats", e);
            throw new RuntimeException("Error fetching stats: " + e.getMessage());
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PostModel>> getRecentPosts(@RequestParam(defaultValue = "10") int limit) {
        log.info("Admin fetching recent posts, limit: {}", limit);

        Page<Post> posts = postRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<PostModel> postModels = PostMapper.toModelList(posts.getContent());
        return ResponseEntity.ok(postModels);
    }
}