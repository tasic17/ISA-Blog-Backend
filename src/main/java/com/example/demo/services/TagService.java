package com.example.demo.services;

import com.example.demo.entities.Tag;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.mappers.TagMapper;
import com.example.demo.models.TagModel;
import com.example.demo.repositories.ITagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final ITagRepository tagRepository;

    public List<TagModel> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return TagMapper.toModelList(tags);
    }

    public TagModel getTagById(Integer id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
        return TagMapper.toModel(tag);
    }

    public TagModel createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw new ValidationException("Tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        tag = tagRepository.save(tag);

        return TagMapper.toModel(tag);
    }

    public void deleteTag(Integer id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found");
        }
        tagRepository.deleteById(id);
    }
}