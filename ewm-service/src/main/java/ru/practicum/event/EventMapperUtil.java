package ru.practicum.event;

import lombok.experimental.UtilityClass;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@UtilityClass
public class EventMapperUtil {

    public Event toEvent(NewEventDto newEventDto, Category category, User initiator) {
        Event.EventBuilder event = Event.builder();

        event.annotation(newEventDto.getAnnotation());
        event.category(category);
        event.confirmedRequests(0);
        event.description(newEventDto.getDescription());
        event.eventDate(newEventDto.getEventDate().toInstant(OffsetDateTime.now().getOffset()));
        event.lat(newEventDto.getLocation().getLat());
        event.lon(newEventDto.getLocation().getLon());
        event.paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false);
        event.participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0);
        event.requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true);
        event.title(newEventDto.getTitle());
        event.initiator(initiator);

        return event.build();
    }

    public EventShortDto toEventShortDto(Event event) {
        EventShortDto.EventShortDtoBuilder eventShortDto = EventShortDto.builder();

        eventShortDto.id(event.getId());
        eventShortDto.annotation(event.getAnnotation());
        eventShortDto.category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()));
        eventShortDto.confirmedRequests(event.getConfirmedRequests());
        eventShortDto.eventDate(LocalDateTime.ofInstant(event.getEventDate(), ZoneId.systemDefault()));
        eventShortDto.initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()));
        eventShortDto.paid(event.getPaid());
        eventShortDto.title(event.getTitle());

        if (event.getViews() != null) {
            eventShortDto.views(event.getViews());
        }
        return eventShortDto.build();
    }

    public EventFullDto toEventFullDto(Event event) {
        EventFullDto.EventFullDtoBuilder eventFullDto = EventFullDto.builder();

        eventFullDto.id(event.getId());
        eventFullDto.annotation(event.getAnnotation());
        eventFullDto.category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()));
        eventFullDto.confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests() : 0);
        eventFullDto.createdOn(LocalDateTime.ofInstant(event.getCreatedOn(), ZoneId.systemDefault()));
        eventFullDto.description(event.getDescription());
        eventFullDto.eventDate(LocalDateTime.ofInstant(event.getEventDate(), ZoneId.systemDefault()));
        eventFullDto.initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()));
        eventFullDto.location(new Location(event.getLat(), event.getLon()));
        eventFullDto.paid(event.getPaid() != null ? event.getPaid() : false);
        eventFullDto.participantLimit(event.getParticipantLimit() != null ? event.getParticipantLimit() : 0);
        eventFullDto.publishedOn(event.getPublishedOn() != null ? LocalDateTime.ofInstant(event.getPublishedOn(), ZoneId.systemDefault()) :
                null);
        eventFullDto.requestModeration(event.getRequestModeration() != null ? event.getRequestModeration() : true);
        eventFullDto.state(event.getState().toString());
        eventFullDto.title(event.getTitle());

        if (event.getViews() != null) {
            eventFullDto.views(event.getViews());
        }
        return eventFullDto.build();
    }
}
