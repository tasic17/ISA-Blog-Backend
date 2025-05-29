package com.example.demo.controllers;

import com.example.demo.models.TagModel;
import com.example.demo.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagModel>> getAllTags() {
        List<TagModel> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagModel> getTagById(@PathVariable Integer id) {
        TagModel tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TagModel> createTag(@RequestParam String name) {
        TagModel tag = tagService.createTag(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}