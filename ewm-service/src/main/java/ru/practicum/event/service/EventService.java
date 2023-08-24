package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.model.RequestParamUser;

import javax.servlet.http.HttpServletRequest;
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

}
