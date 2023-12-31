package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.category.dao.CategoryRepository;
import ru.practicum.category.model.Category;
import ru.practicum.dto.HitDto;
import ru.practicum.event.CommentMapperUtil;
import ru.practicum.event.EventMapperUtil;
import ru.practicum.event.State;
import ru.practicum.event.StateComment;
import ru.practicum.event.dao.CommentRepository;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Comment;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.model.RequestParamUser;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.Status;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.model.Request;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import javax.servlet.http.HttpServletRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository repository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final CommentRepository commentRepository;
    private final StatClient statClient;

    @Transactional
    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, long userId) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Category category = categoryRepository.findById((long) newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", newEventDto.getCategory())));

        Event newEvent = EventMapperUtil.toEvent(newEventDto, category, initiator).toBuilder()
                .createdOn(Instant.now())
                .state(State.PENDING).build();
        Event createdEvent = repository.save(newEvent);
        log.info("Пользователем с id {} добавлено событие с id {}", userId, createdEvent.getId());
        return EventMapperUtil.toEventFullDto(createdEvent);
    }

    @Transactional
    @Override
    public EventFullDto updateEventUser(UpdateEventDto updateEventDto, long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("The user is not the initiator of the event.");
        }
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        Event updateEvent = event.toBuilder()
                .annotation(updateEventDto.getAnnotation() != null ? updateEventDto.getAnnotation() : event.getAnnotation())
                .category(updateEventDto.getCategory() != null ?
                        categoryRepository.findById((long) updateEventDto.getCategory())
                                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", updateEventDto.getCategory()))) :
                        event.getCategory())
                .description(updateEventDto.getDescription() != null ? updateEventDto.getDescription() : event.getDescription())
                .eventDate(updateEventDto.getEventDate() != null ? updateEventDto.getEventDate().toInstant(OffsetDateTime.now().getOffset()) :
                        event.getEventDate())
                .lat(updateEventDto.getLocation() != null ? updateEventDto.getLocation().getLat() : event.getLat())
                .lon(updateEventDto.getLocation() != null ? updateEventDto.getLocation().getLon() : event.getLon())
                .paid(updateEventDto.getPaid() != null ? updateEventDto.getPaid() : event.getPaid())
                .participantLimit(updateEventDto.getParticipantLimit() != null ? updateEventDto.getParticipantLimit() :
                        event.getParticipantLimit())
                .requestModeration(updateEventDto.getRequestModeration() != null ? updateEventDto.getRequestModeration() :
                        event.getRequestModeration())
                .title(updateEventDto.getTitle() != null ? updateEventDto.getTitle() : event.getTitle()).build();
        if (updateEventDto.getStateAction() == null) {
            log.info("Обновлено событие с id {}", eventId);
            return EventMapperUtil.toEventFullDto(repository.save(updateEvent));
        }

        Event resultUpdateEvent = updateEvent;
        if (updateEventDto.getStateAction().equals("SEND_TO_REVIEW")) {
            resultUpdateEvent = updateEvent.toBuilder()
                    .state(State.PENDING).build();
        }
        if (updateEventDto.getStateAction().equals("CANCEL_REVIEW")) {
            resultUpdateEvent = updateEvent.toBuilder()
                    .state(State.CANCELED).build();
        }

        log.info("Пользователем с id {} обновлено созданное им событие с id {}. Обновленные данные сохранены",
                userId, eventId);

        EventFullDto eventFullDto = EventMapperUtil.toEventFullDto(repository.save(resultUpdateEvent));

        return eventFullDto.toBuilder()
                .comments(this.eventCommentsFiller(eventId)).build();
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(UpdateEventDto updateEventDto, long eventId) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));


        if (updateEventDto.getStateAction() != null && updateEventDto.getStateAction().equals("PUBLISH_EVENT") && !event.getState().equals(State.PENDING)) {
            throw new ConflictException("Cannot publish the event because it's not in the right state: PUBLISHED");
        }
        if (updateEventDto.getStateAction() != null && updateEventDto.getStateAction().equals("REJECT_EVENT") && event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("The event cannot be rejected because it has the state: PUBLISHED");
        }

        Event updateEvent = event.toBuilder()
                .annotation(updateEventDto.getAnnotation() != null ? updateEventDto.getAnnotation() : event.getAnnotation())
                .category(updateEventDto.getCategory() != null ?
                        categoryRepository.findById((long) updateEventDto.getCategory())
                                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", updateEventDto.getCategory()))) :
                        event.getCategory())
                .description(updateEventDto.getDescription() != null ? updateEventDto.getDescription() : event.getDescription())
                .eventDate(updateEventDto.getEventDate() != null ? updateEventDto.getEventDate().toInstant(OffsetDateTime.now().getOffset()) :
                        event.getEventDate())
                .lat(updateEventDto.getLocation() != null ? updateEventDto.getLocation().getLat() : event.getLat())
                .lon(updateEventDto.getLocation() != null ? updateEventDto.getLocation().getLon() : event.getLon())
                .paid(updateEventDto.getPaid() != null ? updateEventDto.getPaid() : event.getPaid())
                .participantLimit(updateEventDto.getParticipantLimit() != null ? updateEventDto.getParticipantLimit() :
                        event.getParticipantLimit())
                .requestModeration(updateEventDto.getRequestModeration() != null ? updateEventDto.getRequestModeration() :
                        event.getRequestModeration())
                .title(updateEventDto.getTitle() != null ? updateEventDto.getTitle() : event.getTitle()).build();
        if (updateEventDto.getStateAction() == null) {

            return EventMapperUtil.toEventFullDto(repository.save(updateEvent));
        }
        Event resultUpdateEvent = updateEvent;
        if (updateEventDto.getStateAction() != null && updateEventDto.getStateAction().equals("PUBLISH_EVENT")) {
            resultUpdateEvent = updateEvent.toBuilder()
                    .state(State.PUBLISHED)
                    .publishedOn(Instant.now()).build();

        }
        if (updateEventDto.getStateAction() != null && updateEventDto.getStateAction().equals("REJECT_EVENT")) {
            resultUpdateEvent = updateEvent.toBuilder()
                    .state(State.CANCELED).build();
        }

        log.info("Администратором обновлено событие с id {}. Обновленные данные сохранены", eventId);
        EventFullDto eventFullDto = EventMapperUtil.toEventFullDto(repository.save(resultUpdateEvent));

        return eventFullDto.toBuilder()
                .comments(this.eventCommentsFiller(eventId)).build();
    }

    @Override
    public List<EventFullDto> findAdminEventsByParameters(RequestParamAdmin parameters) {
        if (parameters.getRangeEnd() != null && parameters.getRangeStart() != null &&
                parameters.getRangeEnd().isBefore(parameters.getRangeStart())) {
            throw new ValidationException("The time of the event request period is set incorrectly.");
        }
        Pageable pageable = PageRequest.of(parameters.getFrom() > 0 ? parameters.getFrom() / parameters.getSize()
                : 0, parameters.getSize());

        Page<Event> resultList = repository.findEventsByParametersAdmin(pageable, parameters);

        log.info("Администратором запрошены данные по событиям с заданными параметрами фильтрации. Данные получены");
        return resultList.stream()
                .map(EventMapperUtil::toEventFullDto)
                .peek(ev -> ev.toBuilder().views(this.getHitsById(ev.getId())).build())
                .peek(ev -> ev.toBuilder().comments(this.eventCommentsFiller(ev.getId())).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<EventShortDto> findEventsUser(long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        log.info("Запрошена информация о событиях, созданных пользователем с id {}. Данные получены", userId);
        return repository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapperUtil::toEventShortDto)
                .peek(ev -> ev.toBuilder().comments(this.eventCommentsFiller(ev.getId())).build())
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto findFullEventUser(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (event.getInitiator().getId() != userId) {
            throw new ValidationException(String.format("The user with id %d is not the initiator of the event with id %d",
                    userId, eventId));
        }
        log.info("Пользователь с id {} запросил полную информацию о созданном им событии с id {}. Данные получены",
                userId, eventId);
        return EventMapperUtil.toEventFullDto(event).toBuilder()
                .comments(this.eventCommentsFiller(eventId)).build();
    }

    @Override
    public EventFullDto findEventById(long id, HttpServletRequest request) {
        Event event = repository.findByIdAndStatePublished(id, "PUBLISHED");
        if (event == null) {
            throw new NotFoundException(String.format("Published event with id=%d was not found", id));
        }

        addHit(request);
        Event updateEvent = event.toBuilder().views(this.getHitsById(id)).build();

        log.info("В публичном эндпоинте запрошена полная информация о событии с id {}. Данные получены", id);
        return EventMapperUtil.toEventFullDto(repository.save(updateEvent)).toBuilder()
                .comments(this.eventCommentsFiller(id)).build();
    }

    @Override
    public List<EventShortDto> findPublicEventsByParameters(RequestParamUser parameters, HttpServletRequest request) {
        if (parameters.getRangeEnd() != null && parameters.getRangeStart() != null &&
                parameters.getRangeEnd().isBefore(parameters.getRangeStart())) {
            throw new ValidationException("The time of the event request period is set incorrectly.");
        }
        Pageable pageable = PageRequest.of(parameters.getFrom() > 0 ? parameters.getFrom() / parameters.getSize()
                : 0, parameters.getSize());

        Page<Event> resultList = repository.findEventsByParametersUser(pageable, parameters);

        addHit(request);

        log.info("В публичном эндпоинте запрошены данные по событиям с заданными параметрами фильтрации. Данные получены");
        return resultList.stream()
                .map(EventMapperUtil::toEventShortDto)
                .peek(ev -> ev.toBuilder().views(this.getHitsById(ev.getId())).build())
                .peek(ev -> ev.toBuilder().comments(this.eventCommentsFiller(ev.getId())).build())
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto updateCommentStatus(CommentStatusUpdateDto updateCommentDto, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        Comment updateComment;

        if (!comment.getStateComment().equals(StateComment.PENDING)) {
            throw new ConflictException("Can only change the status of a comment in the waiting state.");
        }

        if (updateCommentDto.getStateComment().equals("PUBLISH_COMMENT")) {
            Comment updateStatusComment = comment.toBuilder()
                    .stateComment(StateComment.PUBLISHED)
                    .publishedOn(Instant.now()).build();
            updateComment = commentRepository.save(updateStatusComment);
        } else if (updateCommentDto.getStateComment().equals("REJECT_COMMENT")) {
            Comment updateStatusComment = comment.toBuilder()
                    .stateComment(StateComment.REJECTED).build();
            updateComment = commentRepository.save(updateStatusComment);
        } else {
            updateComment = comment;
        }

        return CommentMapperUtil.toCommentDto(updateComment);
    }

    @Override
    public Collection<CommentDto> findCommentsByEventPublic(long eventId, Integer from, Integer size) {
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return commentRepository.findAllByEventIdAndStatePublished(eventId, "PUBLISHED", pageable).stream()
                .map(CommentMapperUtil::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto addComment(NewCommentDto newCommentDto, long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (event.getPublishedOn() == null) {
            throw new ConflictException("Can only comment on published events.");
        }

        Request request = requestRepository.findByRequesterAndEvent(userId, eventId);
        if (!request.getStatus().equals(Status.CONFIRMED)) {
            throw new ConflictException("Only the user whose participation request has been confirmed can comment on the event.");
        }

        Comment newComment = commentRepository.save(CommentMapperUtil.toComment(newCommentDto, user, event));
        return CommentMapperUtil.toCommentDto(newComment);
    }

    @Transactional
    @Override
    public CommentDto updateComment(UpdateCommentDtoUser updateCommentDto, long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        if (userId != comment.getAuthor().getId()) {
            throw new ConflictException("Only the author can change the comment.");
        }
        if (comment.getStateComment().equals(StateComment.PUBLISHED)) {
            throw new ConflictException("Cannot change a comment that has already been published.");
        }

        Comment updateComment = comment.toBuilder()
                .text(updateCommentDto.getText()).build();
        return CommentMapperUtil.toCommentDto(commentRepository.save(updateComment));
    }

    @Override
    public void deleteComment(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        if (userId != comment.getAuthor().getId()) {
            throw new ConflictException("Only the author of the comment can delete it.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> findCommentsByEventUser(long userId, long eventId, Integer from, Integer size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Event event = repository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));

        if (userId != event.getInitiator().getId()) {
            throw new ConflictException("Only the initiator of the event can request comments on the event.");
        }
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);

        return commentRepository.findAllByEventId(eventId, pageable).stream()
                .map(CommentMapperUtil::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto findCommentUser(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id=%d was not found", commentId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));

        if (userId != comment.getAuthor().getId()) {
            throw new ConflictException("Only the author can request information on his comment.");
        }
        return CommentMapperUtil.toCommentDto(comment);
    }

    private Integer getHitsById(Long eventId) {
        List<String> uris = new ArrayList<>();

        uris.add("/events/" + eventId);

        ResponseEntity<Object> views = statClient
                .getStats(LocalDateTime.now().minusMinutes(1), LocalDateTime.now(), uris, true);

        String[] body = Objects.requireNonNull(views.getBody()).toString().split("hits=");
        if (!body[0].equals("[]")) {
            return Integer.valueOf(body[1].replaceAll("[^0-9]+", ""));
        } else {
            return 0;
        }
    }

    private void addHit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        HitDto.HitDtoBuilder hitDto = HitDto.builder();
        hitDto.app("ewm-service");
        hitDto.uri(path);
        hitDto.ip(ip);
        hitDto.timestamp(LocalDateTime.now());
        statClient.addPost(hitDto.build());
    }

    private List<CommentDto> eventCommentsFiller(long eventId) {
        Pageable pageable = PageRequest.of(0, 10);

        return commentRepository.findAllByEventIdAndStatePublished(eventId, "PUBLISHED", pageable).stream()
                .map(CommentMapperUtil::toCommentDto)
                .collect(Collectors.toList());
    }
}
