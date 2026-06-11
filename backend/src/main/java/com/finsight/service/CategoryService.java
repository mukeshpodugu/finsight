package com.finsight.service;

import com.finsight.dto.CategoryRequest;
import com.finsight.entity.Category;
import com.finsight.entity.User;
import com.finsight.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategoriesForUser(User user) {
        return categoryRepository.findAllByUserOrSystem(user);
    }

    public Category createCategory(CategoryRequest request, User user) {
        if (categoryRepository.findByNameAndType(request.getName(), request.getType(), user).isPresent()) {
            throw new RuntimeException("Category already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType().toUpperCase())
                .colorCode(request.getColorCode() != null ? request.getColorCode() : "#cccccc")
                .iconName(request.getIconName() != null ? request.getIconName() : "folder")
                .user(user)
                .build();

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id, User user) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (category.getUser() == null) {
            throw new RuntimeException("Cannot delete default system categories");
        }

        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to delete this category");
        }

        categoryRepository.delete(category);
    }
}
