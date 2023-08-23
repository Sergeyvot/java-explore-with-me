package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto createRequest(long userId, long eventId);

    ParticipationRequestDto cancelRequest(long userId, long requestId);

    List<ParticipationRequestDto> findRequests(long userId);

    List<ParticipationRequestDto> findEventUserRequests(long userId, long eventId);

    EventRequestStatusUpdateResult updateEventUserRequests(EventRequestStatusUpdateRequest updateEventDto,
                                                           long userId, long eventId);
}
