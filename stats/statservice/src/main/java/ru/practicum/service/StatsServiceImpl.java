package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatMapperUtil;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHit;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;

    @Override
    public void addPost(HitDto hitDto) {

        EndpointHit newEndpointHit = StatMapperUtil.toEndpointHit(hitDto);
        repository.save(newEndpointHit);
    }

    @Override
    public List<EndpointHit> findAll() {
        return repository.findAll();
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start) || start.equals(end)) {
            log.error("Время запроса задано некорректно.");
            throw new ValidationException("Время запроса задано некорректно.");
        }

        Instant startInstant = start.toInstant(OffsetDateTime.now().getOffset());
        Instant endInstant = end.toInstant(OffsetDateTime.now().getOffset());

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return repository.getStatsWithoutUriWithUniqueIp(startInstant, endInstant).stream()
                        .map(StatMapperUtil::toViewStatsDto)
                        .collect(Collectors.toList());
            } else {
                return repository.getStatsWithoutUri(startInstant, endInstant).stream()
                        .map(StatMapperUtil::toViewStatsDto)
                        .collect(Collectors.toList());
            }
        } else {
            if (unique) {
                return repository.getStatsByUriWithUniqueIp(uris, startInstant, endInstant).stream()
                        .map(StatMapperUtil::toViewStatsDto)
                        .collect(Collectors.toList());
            } else {
                return repository.getStatsByUri(uris, startInstant, endInstant).stream()
                        .map(StatMapperUtil::toViewStatsDto)
                        .collect(Collectors.toList());
            }
        }
    }
}
