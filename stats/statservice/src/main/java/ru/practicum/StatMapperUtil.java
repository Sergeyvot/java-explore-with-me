package ru.practicum;

import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.OffsetDateTime;

public final class StatMapperUtil {

    private StatMapperUtil() {
    }

    public static EndpointHit toEndpointHit(HitDto hitDto) {
        EndpointHit.EndpointHitBuilder endpointHit = EndpointHit.builder();

        if (hitDto.getId() != null) {
            endpointHit.id(hitDto.getId());
        }
        endpointHit.app(hitDto.getApp());
        endpointHit.uri(hitDto.getUri());
        endpointHit.ip(hitDto.getIp());
        endpointHit.timestamp(hitDto.getTimestamp().toInstant(OffsetDateTime.now().getOffset()));

        return endpointHit.build();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        ViewStatsDto.ViewStatsDtoBuilder viewStatsDto = ViewStatsDto.builder();

        viewStatsDto.app(viewStats.getApp());
        viewStatsDto.uri(viewStats.getUri());
        viewStatsDto.hits(viewStats.getHits());

        return viewStatsDto.build();
    }
}
