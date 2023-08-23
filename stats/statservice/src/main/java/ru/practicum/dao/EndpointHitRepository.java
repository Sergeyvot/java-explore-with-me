package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.Instant;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.ip)) from EndpointHit as eh " +
            "where eh.uri in (?1) and eh.timestamp >= ?2 and eh.timestamp <= ?3 group by eh.uri, eh.app order by count(eh.ip) desc")
    List<ViewStats> getStatsByUri(List<String> uris, Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) from EndpointHit as eh where " +
            "eh.uri in (?1) and eh.timestamp >= ?2 and eh.timestamp <= ?3 group by eh.uri, eh.app " +
            "order by count(distinct eh.ip) desc")
    List<ViewStats> getStatsByUriWithUniqueIp(List<String> uris, Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.ip)) from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <= ?2 group by eh.uri, eh.app order by count(eh.ip) desc")
    List<ViewStats> getStatsWithoutUri(Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <= ?2 group by eh.uri, eh.app order by count(distinct eh.ip) desc")
    List<ViewStats> getStatsWithoutUriWithUniqueIp(Instant start, Instant end);
}
