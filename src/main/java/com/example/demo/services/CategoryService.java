package com.example.demo.services;

import com.example.demo.entities.Category;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.mappers.CategoryMapper;
import com.example.demo.models.CategoryModel;
import com.example.demo.repositories.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ICategoryRepository categoryRepository;

    public List<CategoryModel> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return CategoryMapper.toModelList(categories);
    }

    public CategoryModel getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return CategoryMapper.toModel(category);
    }

    public CategoryModel createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new ValidationException("Category already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category = categoryRepository.save(category);

        return CategoryMapper.toModel(category);
    }

    public CategoryModel updateCategory(Integer id, String name, String description) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(name);
        category.setDescription(description);
        category = categoryRepository.save(category);

        return CategoryMapper.toModel(category);
    }

    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}