package ru.practicum.event_compilation.service;

import ru.practicum.event_compilation.model.EventCompilation;
import ru.practicum.event_compilation.model.EventId;

import java.util.List;

public interface EventCompilationService {

    List<EventId> findEventIds(long compId);

    void deleteByCompilationId(long compId);

    EventCompilation saveEventCompilation(Long eventId, Long compId);
}
