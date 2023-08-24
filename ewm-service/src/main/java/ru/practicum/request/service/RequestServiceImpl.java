package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dao.EventRepository;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.RequestMapperUtil;
import ru.practicum.request.Status;
import ru.practicum.request.dao.RequestRepository;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        User checkUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        Request checkRequest = repository.findByRequesterAndEvent(userId, eventId);
        if (checkRequest != null) {
            throw new ConflictException("Cannot add a repeat request.");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (userId == event.getInitiator().getId()) {
            throw new ConflictException("The initiator of the event cannot apply for participation in it.");
        }
        if (event.getPublishedOn() == null) {
            throw new ConflictException("Cannot participate in an unpublished event.");
        }
        if (event.getConfirmedRequests().equals(event.getParticipantLimit()) && event.getParticipantLimit() != 0) {
            throw new ConflictException("The limit of requests for participation in the event is filled.");
        }

        Request.RequestBuilder request = Request.builder();
        request.requester(userId);
        request.event(eventId);
        request.created(LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset()));
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.status(Status.CONFIRMED);
            Event updateEvent = event.toBuilder()
                    .confirmedRequests(event.getConfirmedRequests() + 1).build();
            eventRepository.save(updateEvent);
        } else {
            request.status(Status.PENDING);
        }
        log.info("Добавлена заявка на участие в событии с id {}.", eventId);
        return RequestMapperUtil.toParticipationRequestDto(repository.save(request.build()));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id=%d was not found", requestId)));
        if (request.getRequester() != userId) {
            throw new ValidationException(String.format("The user id %d does not match the id of the requester %d",
                    userId, request.getRequester()));
        }

        Request updateRequest = request.toBuilder()
                .status(Status.CANCELED).build();
        log.info("Заявка на участие в событии с id {} отменена инициатором заявки", requestId);
        return RequestMapperUtil.toParticipationRequestDto(repository.save(updateRequest));
    }

    @Override
    public List<ParticipationRequestDto> findRequests(long userId) {
        User checkUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        List<Request> requests = repository.findByRequester(userId);
        log.info("Пользователем с id {} запрошены свои заявки на участие в событиях", userId);
        return requests.stream()
                .map(RequestMapperUtil::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventUserRequests(EventRequestStatusUpdateRequest updateEventDto,
                                                                  long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (event.getParticipantLimit() == 0 || (!event.getRequestModeration())) {
            throw new ValidationException(String.format("Confirmation of requests to participate in an event with id %d " +
                    "is not required", eventId));
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("The user is not the initiator of the event.");
        }
        if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            throw new ConflictException("The limit of requests for participation in the event is filled.");
        }
        List<Long> requestIds = updateEventDto.getRequestIds().stream()
                .map(Long::valueOf).collect(Collectors.toList());
        List<Request> requests = repository.findByRequesters(requestIds);
        Integer updateConfirmedRequests = event.getConfirmedRequests();
        for (Request request : requests) {
            if (request.getStatus().equals(Status.CONFIRMED) && updateEventDto.getStatus().equals("REJECTED")) {
                throw new ConflictException("cannot cancel an already accepted request.");
            }
            if (!request.getStatus().equals(Status.PENDING)) {
                throw new ConflictException("Can change the status only for requests that are in the pending state.");
            }
            if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
                throw new ConflictException("The limit of requests for participation in the event is filled.");
            }
            Request updateRequest = request.toBuilder()
                    .status(Status.valueOf(updateEventDto.getStatus())).build();
            repository.save(updateRequest);
            if (updateEventDto.getStatus().equals("CONFIRMED")) {
                updateConfirmedRequests++;
                if (updateConfirmedRequests.equals(event.getParticipantLimit())) {
                    List<Request> pendingRequests = repository.findAllByEventAndStatus(eventId, "PENDING");
                    for (Request request1 : pendingRequests) {
                        Request updateRequest1 = request1.toBuilder()
                                .status(Status.REJECTED).build();
                        repository.save(updateRequest1);
                    }
                    Event updateEvent = event.toBuilder()
                            .confirmedRequests(updateConfirmedRequests).build();
                    eventRepository.save(updateEvent);
                }
            }
        }
        Event updateEvent = event.toBuilder()
                .confirmedRequests(updateConfirmedRequests).build();
        eventRepository.save(updateEvent);
        List<ParticipationRequestDto> confirmedRequests = repository.findAllByEventAndStatus(eventId, "CONFIRMED")
                .stream().map(RequestMapperUtil::toParticipationRequestDto).collect(Collectors.toList());
        List<ParticipationRequestDto> rejectedRequests = repository.findAllByEventAndStatus(eventId, "REJECTED")
                .stream().map(RequestMapperUtil::toParticipationRequestDto).collect(Collectors.toList());
        EventRequestStatusUpdateResult.EventRequestStatusUpdateResultBuilder result = EventRequestStatusUpdateResult.builder();
        result.confirmedRequests(confirmedRequests);
        result.rejectedRequests(rejectedRequests);
        log.info("Пользователь с id {} изменил статусы заявок на участие в событии id {}", userId, eventId);
        return result.build();
    }

    @Override
    public List<ParticipationRequestDto> findEventUserRequests(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ValidationException("The user is not the initiator of the event.");
        }
        log.info("Пользователь с id {} запросил данные по заявкам на участие в созданном им событии id {}", userId, eventId);
        return repository.findAllByEvent(eventId).stream()
                .map(RequestMapperUtil::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
