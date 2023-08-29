package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @PostMapping("/users")
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
        UserDto result = userService.createUser(userDto);
        if (result != null) {
            log.info("В приложение добавлен пользователь с id {}", result.getId());
        } else {
            log.info("Добавление в приложение пользователя не выполнено. Необходимо определить ошибку");
        }
        return result;
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("userId") long userId) {
        userService.removeUser(userId);

        log.info("Удален пользователь с id {}", userId);
    }

    @GetMapping("/users")
    public Collection<UserDto> findUsers(@RequestParam(name = "ids", required = false) List<Integer> ids,
                                         @RequestParam(defaultValue = "0", required = false) Integer from,
                                         @RequestParam(defaultValue = "10", required = false) Integer size) {
        List<UserDto> resultList;
        if (ids == null || ids.isEmpty()) {
            resultList = userService.findAllUsers(from, size);
        } else {
            resultList = userService.findUsersById(ids, from, size);
        }

        if (resultList != null) {
            log.info("Запрошен список всех пользователей приложения. Данные получены");
        } else {
            log.info("Запрос списка всех пользователей приложения не выполнен. Необходимо определить ошибку");
        }
        return resultList;
    }

    @PostMapping("/categories")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CategoryDto addCategory(@RequestBody @Valid CategoryDto categoryDto) {
        CategoryDto result = categoryService.addCategory(categoryDto);
        if (result != null) {
            log.info("В приложение добавлена категория с id {}", result.getId());
        } else {
            log.info("Добавление в приложение категории не выполнено. Необходимо определить ошибку");
        }
        return result;
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable("catId") int catId) {
        categoryService.removeCategory(catId);

        log.info("Удалена категория с id {}", catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@RequestBody @Valid CategoryDto categoryDto,
                                      @PathVariable("catId") int catId) {
        CategoryDto result = categoryService.updateCategory(catId, categoryDto);
        if (result != null) {
            log.info("Обновлена категория с id {}", catId);
        } else {
            log.info("Обновление категории с id {} не выполнено. Необходимо определить ошибку", catId);
        }
        return result;
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEventAdmin(@RequestBody @Valid UpdateEventDto updateEventDto,
                                         @PositiveOrZero @PathVariable("eventId") int eventId) {
        if (updateEventDto.getEventDate() != null &&
                updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                    "Value: %s", updateEventDto.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        EventFullDto result = eventService.updateEventAdmin(updateEventDto, eventId);
        if (result != null) {
            log.info("Обновлено событие с id {}", eventId);
        } else {
            log.info("Обновление события с id {} не выполнено. Необходимо определить ошибку", eventId);
        }
        return result;
    }

    @GetMapping("/events")
    public Collection<EventFullDto> findAdminEvents(@RequestParam(name = "users", required = false) List<Integer> users,
                                                    @RequestParam(name = "states", required = false) List<String> states,
                                                    @RequestParam(name = "categories", required = false) List<Integer> categories,
                                                    @RequestParam(name = "rangeStart", required = false)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                    @RequestParam(name = "rangeEnd", required = false)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                    @RequestParam(defaultValue = "0", required = false) Integer from,
                                                    @RequestParam(defaultValue = "10", required = false) Integer size) {

        RequestParamAdmin.RequestParamAdminBuilder parameters = RequestParamAdmin.builder();
        parameters.users(users);
        parameters.states(states);
        parameters.categories(categories);
        parameters.rangeStart(rangeStart);
        parameters.rangeEnd(rangeEnd);
        parameters.from(from);
        parameters.size(size);

        List<EventFullDto> resultList = eventService.findAdminEventsByParameters(parameters.build());

        log.info("Запрошен список событий, удовлетворяющих запрошенным параметрам. Данные получены");
        return resultList;
    }

    @PostMapping("/compilations")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        CompilationDto result = compilationService.addCompilation(newCompilationDto);
        if (result != null) {
            log.info("В приложение добавлена подборка событий с id {}", result.getId());
        } else {
            log.info("Добавление в приложение подборки событий не выполнено. Необходимо определить ошибку");
        }
        return result;
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PositiveOrZero @PathVariable("compId") int compId) {

        compilationService.removeCompilation(compId);
        log.info("Удалена подборка событий с id {}", compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@RequestBody @Valid UpdateCompilationRequest updateCompilationDto,
                                            @PositiveOrZero @PathVariable("compId") int compId) {
        CompilationDto result = compilationService.updateCompilation(updateCompilationDto, compId);
        if (result != null) {
            log.info("Обновлена подборка событий с id {}", compId);
        } else {
            log.info("Обновление подборки событий с id {} не выполнено. Необходимо определить ошибку", compId);
        }
        return result;
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateCommentStatus(@RequestBody @Valid CommentStatusUpdateDto updateCommentDto,
                                    @PositiveOrZero @PathVariable("commentId") long commentId) {

        CommentDto result = eventService.updateCommentStatus(updateCommentDto, commentId);
        if (result != null) {
            log.info("Изменен статус комментария с id {}", commentId);
        } else {
            log.info("Изменение статуса комментария с id {} не выполнено. Необходимо определить ошибку", commentId);
        }
        return result;
    }
}
