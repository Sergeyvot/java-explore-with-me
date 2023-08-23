package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.CompilationMapperUtil;
import ru.practicum.compilation.dao.CompilationRepository;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.EventMapperUtil;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;
import ru.practicum.event_compilation.model.EventCompilation;
import ru.practicum.event_compilation.model.EventId;
import ru.practicum.event_compilation.service.EventCompilationService;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository repository;
    private final EventCompilationService eventCompilationService;
    private final EventService eventService;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = repository.save(CompilationMapperUtil.toCompilation(newCompilationDto));
        List<EventShortDto> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            List<Long> eventIds = newCompilationDto.getEvents().stream()
                    .map(Long::valueOf).collect(Collectors.toList());

            for (Long id : eventIds) {
                EventCompilation eventCompilation = eventCompilationService.saveEventCompilation(id, compilation.getId());
                events.add(EventMapperUtil.toEventShortDto(eventService.findById(id)));
            }
        }

        log.info("В приложение добавлена подборка событий с id {}", compilation.getId());
        return CompilationMapperUtil.toCompilationDto(compilation)
                .toBuilder().events(events).build();
    }

    @Override
    public void removeCompilation(long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        log.info("Из приложения удалена подборка событий с id {}", compId);
        repository.deleteById(compId);
        eventCompilationService.deleteByCompilationId(compId);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationDto, long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));

        List<EventShortDto> events = new ArrayList<>();
        Compilation updateCompilation = compilation.toBuilder()
                .title(updateCompilationDto.getTitle() != null ? updateCompilationDto.getTitle() : compilation.getTitle())
                .pinned(updateCompilationDto.getPinned() != null ? updateCompilationDto.getPinned() : compilation.getPinned())
                .build();

        if (updateCompilationDto.getEvents() != null) {
            eventCompilationService.deleteByCompilationId(compId);
            List<Long> eventIds = updateCompilationDto.getEvents().stream()
                    .map(Long::valueOf).collect(Collectors.toList());

            for (Long id : eventIds) {
                eventCompilationService.saveEventCompilation(id, compId);
                events.add(EventMapperUtil.toEventShortDto(eventService.findById(id)));
            }
            log.info("Обновлена подборка событий с id {}. Обновленные данные сохранены", compId);
            return CompilationMapperUtil.toCompilationDto(updateCompilation).toBuilder().events(events).build();
        } else {
            List<EventId> eventsIds = eventCompilationService.findEventIds(compId);
            List<Long> eventIds = eventsIds.stream()
                    .map(EventId::getEventId)
                    .collect(Collectors.toList());

            for (Long id : eventIds) {
                events.add(EventMapperUtil.toEventShortDto(eventService.findById(id)));
            }
            log.info("Обновлена подборка событий с id {}. Обновленные данные сохранены", compId);
            return CompilationMapperUtil.toCompilationDto(updateCompilation).toBuilder().events(events).build();
        }
    }

    @Override
    public List<CompilationDto> findCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Compilation> compilations;
        if (pinned != null) {
            compilations = repository.findAllByPinned(pinned, pageable);
        } else {
            compilations = repository.findAll(pageable);
        }

        List<CompilationDto> compilationDtoList = compilations.stream()
                .map(CompilationMapperUtil::toCompilationDto)
                .collect(Collectors.toList());
        List<CompilationDto> result = new ArrayList<>();
        for (CompilationDto compilationDto : compilationDtoList) {
            result.add(compilationFiller(compilationDto));
        }

        log.info("Запрошен список подборок событий по заданным фильтрам. Данные получены");
        return result;
    }

    @Override
    public CompilationDto findCompilationById(long compId) {
        Compilation compilation = repository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id=%d was not found", compId)));
        CompilationDto compilationDto = CompilationMapperUtil.toCompilationDto(compilation);

        log.info("Запрошена информация по подборке событий с id {}. Данные получены", compId);
        return compilationFiller(compilationDto);
    }

    private CompilationDto compilationFiller(CompilationDto compilationDto) {
        List<EventId> eventsIds = eventCompilationService.findEventIds(compilationDto.getId());
        List<Long> eventId = eventsIds.stream()
                .map(EventId::getEventId)
                .collect(Collectors.toList());
        List<EventShortDto> events = new ArrayList<>();
        for (Long id : eventId) {
            events.add(EventMapperUtil.toEventShortDto(eventService.findById(id)));
        }
        CompilationDto.CompilationDtoBuilder compilation = CompilationDto.builder();
        compilation.id(compilationDto.getId());
        compilation.pinned(compilationDto.getPinned());
        compilation.title(compilationDto.getTitle());
        compilation.events(events);
        return compilation.build();
    }
}
