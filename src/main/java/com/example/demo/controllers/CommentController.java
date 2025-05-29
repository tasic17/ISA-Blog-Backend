package com.example.demo.controllers;

import com.example.demo.models.CommentCreateModel;
import com.example.demo.models.CommentModel;
import com.example.demo.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentModel>> getPostComments(@PathVariable Integer postId) {
        List<CommentModel> comments = commentService.getPostComments(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/pageable")
    public ResponseEntity<Page<CommentModel>> getPostCommentsPageable(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CommentModel> comments = commentService.getPostCommentsPageable(postId, page, size);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentModel> createComment(@Valid @RequestBody CommentCreateModel model) {
        CommentModel comment = commentService.createComment(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentModel> updateComment(
            @PathVariable Integer id,
            @RequestParam String content
    ) {
        CommentModel comment = commentService.updateComment(id, content);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveComment(
            @PathVariable Integer id,
            @RequestParam boolean approve
    ) {
        commentService.approveComment(id, approve);
        return ResponseEntity.ok().build();
    }
}