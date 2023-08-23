package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.StatClient;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.RequestParamUser;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicController {
    private final CategoryService categoryService;
    private final CompilationService compilationService;
    private final EventService eventService;
    private final StatClient statClient;

    @GetMapping("/categories")
    public Collection<CategoryDto> findAllCategories(@RequestParam(defaultValue = "0", required = false) Integer from,
                                                     @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Запрошен список всех категорий приложения. Данные получены");

        return categoryService.findAllCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto findCategoryById(@PositiveOrZero @PathVariable("catId") int catId) {
        log.info("Запрошена информация по категории с id {}. Данные получены", catId);

        return categoryService.findCategoryById(catId);
    }

    @GetMapping("/compilations")
    public Collection<CompilationDto> findCompilations(@RequestParam(required = false) Boolean pinned,
                                                       @RequestParam(defaultValue = "0", required = false) Integer from,
                                                       @RequestParam(defaultValue = "10", required = false) Integer size) {
        log.info("Запрошен список подборок событий по заданным фильтрам. Данные получены");

        return compilationService.findCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto findCompilationById(@PositiveOrZero @PathVariable("compId") int compId) {
        log.info("Запрошена подборка событий с id {}. Данные получены", compId);

        return compilationService.findCompilationById(compId);
    }

    @GetMapping("/events/{id}")
    public EventFullDto findEventById(@PositiveOrZero @PathVariable("id") int id, HttpServletRequest request) {

        EventFullDto result = eventService.findEventById(id, request);

        if (result != null) {
            log.info("Запрошена полная информация о событии с id {}. Данные получены", id);
        } else {
            log.info("Получение информации о событии с id {} не выполнено. Необходимо определить ошибку", id);
        }
        return result;
    }

    @GetMapping("/events")
    public List<EventShortDto> findPublicEventsByParameters(HttpServletRequest request,
                                                            @RequestParam(required = false) String text,
                                                            @RequestParam(required = false) List<Integer> categories,
                                                            @RequestParam(required = false) Boolean paid,
                                                            @RequestParam(name = "rangeStart", required = false)
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                            @RequestParam(name = "rangeEnd", required = false)
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                            @RequestParam(defaultValue = "false", required = false) Boolean onlyAvailable,
                                                            @RequestParam(required = false) String sort,
                                                            @RequestParam(defaultValue = "0", required = false) Integer from,
                                                            @RequestParam(defaultValue = "10", required = false) Integer size) {

        RequestParamUser.RequestParamUserBuilder parameters = RequestParamUser.builder();
        parameters.text(text);
        parameters.categories(categories);
        parameters.paid(paid);
        parameters.rangeStart(rangeStart);
        parameters.rangeEnd(rangeEnd);
        parameters.onlyAvailable(onlyAvailable);
        parameters.sort(sort);
        parameters.from(from);
        parameters.size(size);

        List<EventShortDto> result = eventService.findPublicEventsByParameters(parameters.build(), request);

        log.info("Запрошена информация о событиях по указанным фильтрам. Данные получены");

        return result;
    }
}
