package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateController {
    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EventFullDto createEvent(@RequestBody @Valid NewEventDto newEventDto,
                                    @PositiveOrZero @PathVariable("userId") int userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                    "Value: %s", newEventDto.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        EventFullDto result = eventService.createEvent(newEventDto, userId);
        if (result != null) {
            log.info("В приложение добавлено событие с id {}", result.getId());
        } else {
            log.info("Добавление в приложение события не выполнено. Необходимо определить ошибку");
        }
        return result;
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEventUser(@RequestBody @Valid UpdateEventDto updateEventDto,
                                        @PositiveOrZero @PathVariable("userId") int userId,
                                        @PositiveOrZero @PathVariable("eventId") int eventId) {
        if (updateEventDto.getEventDate() != null &&
                updateEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. " +
                    "Value: %s", updateEventDto.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        EventFullDto result = eventService.updateEventUser(updateEventDto, userId, eventId);
        if (result != null) {
            log.info("Обновлено событие с id {}", eventId);
        } else {
            log.info("Обновление события с id {} не выполнено. Необходимо определить ошибку", eventId);
        }
        return result;
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public Collection<ParticipationRequestDto> findEventUserRequests(@PositiveOrZero @PathVariable("userId") int userId,
                                                                     @PositiveOrZero @PathVariable("eventId") int eventId) {

        log.info("Пользователем с id {} запрошен список заявок на участие в событии с id {}. Данные получены",
                userId, eventId);

        return requestService.findEventUserRequests(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventUserRequests(@RequestBody @Valid EventRequestStatusUpdateRequest updateEventDto,
                                                                  @PositiveOrZero @PathVariable("userId") int userId,
                                                                  @PositiveOrZero @PathVariable("eventId") int eventId) {

        EventRequestStatusUpdateResult result = requestService.updateEventUserRequests(updateEventDto, userId, eventId);
        if (result != null) {
            log.info("Обновлены заявки на участие в событии с id {}", eventId);
        } else {
            log.info("Обновление заявок на участие в событии с id {} не выполнено. Необходимо определить ошибку", eventId);
        }
        return result;
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PositiveOrZero @PathVariable("userId") int userId,
                                                 @PositiveOrZero @RequestParam int eventId) {

        ParticipationRequestDto result = requestService.createRequest(userId, eventId);
        if (result != null) {
            log.info("Добавлена заявка на участие в событии с id {}", eventId);
        } else {
            log.info("Добавление заявки на участие в событии не выполнено. Необходимо определить ошибку");
        }
        return result;
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PositiveOrZero @PathVariable("userId") int userId,
                                                 @PositiveOrZero @PathVariable("requestId") int requestId) {

        ParticipationRequestDto result = requestService.cancelRequest(userId, requestId);
        if (result != null) {
            log.info("Отменена заявка на участие с id {}", requestId);
        } else {
            log.info("Отмена заявки на участие с id {} не выполнено. Необходимо определить ошибку", requestId);
        }
        return result;
    }

    @GetMapping("/{userId}/requests")
    public Collection<ParticipationRequestDto> findRequests(@PathVariable("userId") int userId) {
        log.info("Пользователем с Id {} запрошен список заявок на участие в чужих событиях. Данные получены", userId);

        return requestService.findRequests(userId);
    }

    @GetMapping("/{userId}/events")
    public Collection<EventShortDto> findEventsUser(@PositiveOrZero @PathVariable("userId") int userId,
                                                    @RequestParam(defaultValue = "0", required = false) Integer from,
                                                    @RequestParam(defaultValue = "10", required = false) Integer size) {

        log.info("Пользователем с id {} запрошен список добавленных им событий. Данные получены", userId);

        return eventService.findEventsUser(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto findFullEventUser(@PositiveOrZero @PathVariable("userId") int userId,
                                          @PositiveOrZero @PathVariable("eventId") int eventId) {

        log.info("Пользователем с id {} запрошена полная информация о добавленном им событии с id {}. Данные получены",
                userId, eventId);

        return eventService.findFullEventUser(userId, eventId);
    }

    @PostMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CommentDto addComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                 @PositiveOrZero @PathVariable("userId") long userId,
                                 @PositiveOrZero @PathVariable("eventId") long eventId) {

        CommentDto result = eventService.addComment(newCommentDto, userId, eventId);
        if (result != null) {
            log.info("Добавлен комментарий с id {} к событию id {}", result.getId(), eventId);
        } else {
            log.info("Добавление комментария к событию с id {} не выполнено. Необходимо определить ошибку", eventId);
        }
        return result;
    }

    @PatchMapping("/{userId}/comments/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid UpdateCommentDtoUser updateCommentDto,
                                    @PositiveOrZero @PathVariable("userId") long userId,
                                    @PositiveOrZero @PathVariable("commentId") long commentId) {

        CommentDto result = eventService.updateComment(updateCommentDto, userId, commentId);
        if (result != null) {
            log.info("Изменен комментарий с id {}.", commentId);
        } else {
            log.info("Изменение комментария с id {} не выполнено. Необходимо определить ошибку", commentId);
        }
        return result;
    }

    @DeleteMapping("/{userId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteComment(@PositiveOrZero @PathVariable("userId") long userId,
                              @PositiveOrZero @PathVariable("commentId") long commentId) {
        eventService.deleteComment(userId, commentId);

        log.info("Удален комментарий с id {}", commentId);
    }

    @GetMapping("/{userId}/events/{eventId}/comments")
    public Collection<CommentDto> findCommentsByEventUser(@PositiveOrZero @PathVariable("userId") int userId,
                                                          @PositiveOrZero @PathVariable("eventId") long eventId,
                                                          @RequestParam(defaultValue = "0", required = false) Integer from,
                                                          @RequestParam(defaultValue = "10", required = false) Integer size) {

        log.info("Пользователем с id {} запрошен список комментариев к созданному им событию id {}. Данные получены",
                userId, eventId);

        return eventService.findCommentsByEventUser(userId, eventId, from, size);
    }

    @GetMapping("/{userId}/comments/{commentId}")
    public CommentDto findCommentUser(@PositiveOrZero @PathVariable("userId") int userId,
                                      @PositiveOrZero @PathVariable("commentId") long commentId) {

        CommentDto result = eventService.findCommentUser(userId, commentId);
        if (result != null) {
            log.info("Пользователем с id {} запрошен свой комментарий id {}. Данные получены",
                    userId, commentId);
        } else {
            log.info("Получение комментария с id {} не выполнено. Необходимо определить ошибку", commentId);
        }
        return result;
    }
}
