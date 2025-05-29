package com.example.demo.controllers;

import com.example.demo.models.PostCreateModel;
import com.example.demo.models.PostModel;
import com.example.demo.models.PostPageModel;
import com.example.demo.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<PostPageModel> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publishedAt") String sortBy
    ) {
        PostPageModel posts = postService.getPublishedPosts(page, size, sortBy);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostModel> getPostBySlug(@PathVariable String slug) {
        PostModel post = postService.getPostBySlug(slug);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostModel> getPostById(@PathVariable Integer id) {
        PostModel post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/search")
    public ResponseEntity<PostPageModel> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PostPageModel posts = postService.searchPosts(q, page, size);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PostPageModel> getPostsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PostPageModel posts = postService.getPostsByCategory(categoryId, page, size);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<PostPageModel> getPostsByTag(
            @PathVariable Integer tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PostPageModel posts = postService.getPostsByTag(tagId, page, size);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<PostPageModel> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String status
    ) {
        PostPageModel posts = postService.getMyPosts(page, size, status);
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<PostModel> createPost(@Valid @RequestBody PostCreateModel model) {
        PostModel post = postService.createPost(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<PostModel> updatePost(
            @PathVariable Integer id,
            @Valid @RequestBody PostCreateModel model
    ) {
        PostModel post = postService.updatePost(id, model);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> toggleLike(@PathVariable Integer id) {
        postService.toggleLike(id);
        return ResponseEntity.ok().build();
    }
}