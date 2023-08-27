package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.model.RequestParamUser;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

public interface EventService {

    EventFullDto createEvent(NewEventDto newEventDto, long userId);

    EventFullDto updateEventUser(UpdateEventDto updateEventDto, long userId, long eventId);

    EventFullDto updateEventAdmin(UpdateEventDto updateEventDto, long eventId);

    List<EventFullDto> findAdminEventsByParameters(RequestParamAdmin parameters);

    List<EventShortDto> findEventsUser(long userId, Integer from, Integer size);

    EventFullDto findFullEventUser(long userId, long eventId);

    EventFullDto findEventById(long id, HttpServletRequest request);

    List<EventShortDto> findPublicEventsByParameters(RequestParamUser parameters, HttpServletRequest request);

    CommentDto updateCommentStatus(CommentStatusUpdateDto updateCommentDto, long commentId);

    Collection<CommentDto> findCommentsByEventPublic(long eventId, Integer from, Integer size);

    CommentDto addComment(NewCommentDto newCommentDto, long userId, long eventId);

    CommentDto updateComment(UpdateCommentDtoUser updateCommentDto, long userId, long commentId);

    void deleteComment(long userId, long commentId);

    List<CommentDto> findCommentsByEventUser(long userId, long eventId, Integer from, Integer size);

    CommentDto findCommentUser(long userId, long commentId);
}
