package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.CategoryMapperUtil;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;

import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public CategoryDto addCategory(CategoryDto categoryDto) {
        Category checkCategory = repository.findByName(categoryDto.getName());
        if (checkCategory != null) {
            throw new ConflictException("The category name must be unique");
        }

        Category category = repository.save(CategoryMapperUtil.toCategory(categoryDto));

        log.info("Добавлена категория с id {}", category.getId());
        return CategoryMapperUtil.toCategoryDto(category);
    }

    @Override
    public void removeCategory(int catId) {
        Long longCatId = (long) catId;
        Category category = repository.findById(longCatId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));

        List<Event> checkList = eventRepository.findAllByCategoryId(longCatId);
        if (checkList != null && !checkList.isEmpty()) {
            throw new ConflictException("Cannot delete a category with linked events");
        }

        log.info("Удалена категория с id {}", catId);
        repository.deleteById((long) catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(int catId, CategoryDto categoryDto) {
        Long longCatId = (long) catId;
        Category category = repository.findById(longCatId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));
        Category checkCategory = repository.findByName(categoryDto.getName());
        if (checkCategory != null && checkCategory.getId() != catId) {
            throw new ConflictException("The category name must be unique");
        }
        Category updateCategory = category.toBuilder()
                .name(categoryDto.getName()).build();

        log.info("Обновлена категория с id {}. Обновленные данные сохранены", catId);
        return CategoryMapperUtil.toCategoryDto(repository.save(updateCategory));
    }

    @Override
    public List<CategoryDto> findAllCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Category> categories = repository.findAll(pageable);

        log.info("Запрошен список всех категорий приложения. Данные получены");
        return categories.stream()
                .map(CategoryMapperUtil::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findCategoryById(int catId) {
        Long longCatId = (long) catId;
        Category category = repository.findById(longCatId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", catId)));

        log.info("Запрошена информвция по категории с id {}. Данные получены", catId);
        return CategoryMapperUtil.toCategoryDto(category);
    }
}
