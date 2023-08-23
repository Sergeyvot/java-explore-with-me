package ru.practicum.category.service;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto addCategory(CategoryDto categoryDto);

    void removeCategory(int categoryId);

    CategoryDto updateCategory(int catId, CategoryDto categoryDto);

    List<CategoryDto> findAllCategories(Integer from, Integer size);

    CategoryDto findCategoryById(int catId);
}
