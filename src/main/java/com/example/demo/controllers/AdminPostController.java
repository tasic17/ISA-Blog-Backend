package com.example.demo.controllers;

import com.example.demo.entities.Post;
import com.example.demo.mappers.PostMapper;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import com.example.demo.repositories.IPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@CrossOrigin("*")
@PreAuthorize("hasRole('ADMIN')")
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
        Sort sort = sortDir.equals("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Page<Post> postPage;

        if (status != null) {
            Post.PostStatus postStatus = Post.PostStatus.valueOf(status.toUpperCase());
            postPage = postRepository.findByStatus(postStatus, PageRequest.of(page, size, sort));
        } else {
            postPage = postRepository.findAll(PageRequest.of(page, size, sort));
        }

        return ResponseEntity.ok(PostMapper.toPageModel(postPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostModel> getPostById(@PathVariable Integer id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return ResponseEntity.ok(PostMapper.toModel(post));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PostModel> updatePostStatus(
            @PathVariable Integer id,
            @RequestParam String status
    ) {
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
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPostStats() {
        long totalPosts = postRepository.count();
        long publishedPosts = postRepository.countByStatus(Post.PostStatus.PUBLISHED);
        long draftPosts = postRepository.countByStatus(Post.PostStatus.DRAFT);
        long archivedPosts = postRepository.countByStatus(Post.PostStatus.ARCHIVED);

        List<Object[]> categoryStats = postRepository.countPostsByCategory();

        Map<String, Object> stats = Map.of(
                "totalPosts", totalPosts,
                "publishedPosts", publishedPosts,
                "draftPosts", draftPosts,
                "archivedPosts", archivedPosts,
                "categoryStats", categoryStats
        );

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PostModel>> getRecentPosts(@RequestParam(defaultValue = "10") int limit) {
        Page<Post> posts = postRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<PostModel> postModels = PostMapper.toModelList(posts.getContent());
        return ResponseEntity.ok(postModels);
    }
}