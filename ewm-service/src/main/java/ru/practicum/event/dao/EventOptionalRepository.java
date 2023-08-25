package ru.practicum.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.model.RequestParamUser;

@Repository
public interface EventOptionalRepository {

    Page<Event> findEventsByParametersUser(Pageable pageable, RequestParamUser parameters);

    Page<Event> findEventsByParametersAdmin(Pageable pageable, RequestParamAdmin parameters);
}
