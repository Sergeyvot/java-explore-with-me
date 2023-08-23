package ru.practicum.event_compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event_compilation.dao.EventCompilationRepository;
import ru.practicum.event_compilation.model.EventCompilation;
import ru.practicum.event_compilation.model.EventId;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCompilationServiceImpl implements EventCompilationService {
    private final EventCompilationRepository repository;

    @Override
    public List<EventId> findEventIds(long compId) {

        log.info("Запрошен список id событий, входящих в подборку с id {}. Данные получены", compId);
        return repository.findAllEventIdByCompilationId(compId);
    }

    @Override
    public void deleteByCompilationId(long compId) {

        log.info("Удалены связанные данные подборки с id {} и входящих в нее событий", compId);
        repository.deleteByCompilationId(compId);
    }

    @Override
    public EventCompilation saveEventCompilation(Long eventId, Long compId) {

        EventCompilation.EventCompilationBuilder eventCompilation = EventCompilation.builder();
        eventCompilation.eventId(eventId);
        eventCompilation.compilationId(compId);

        log.info("Сохранены данные подборки с id {} и входящего в нее события id {}", compId, eventId);
        return repository.save(eventCompilation.build());
    }
}
