package ru.practicum.category;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

@UtilityClass
public class CategoryMapperUtil {

    public Category toCategory(CategoryDto categoryDto) {
        Category.CategoryBuilder category = Category.builder();

        if (categoryDto.getId() != null) {
            category.id(categoryDto.getId());
        }
        category.name(categoryDto.getName());

        return category.build();
    }

    public CategoryDto toCategoryDto(Category category) {
        CategoryDto.CategoryDtoBuilder categoryDto = CategoryDto.builder();

        categoryDto.id(category.getId());
        categoryDto.name(category.getName());

        return categoryDto.build();
    }

}
