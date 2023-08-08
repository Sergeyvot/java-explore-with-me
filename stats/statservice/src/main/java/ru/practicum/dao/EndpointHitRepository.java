package ru.practicum.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;
import ru.practicum.model.ViewStatsUniqueIp;

import java.time.Instant;
import java.util.List;

@Repository
public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.uri)) from EndpointHit as eh " +
            "where eh.uri = concat('', ?1, '') and eh.timestamp >= ?2 and eh.timestamp <= ?3 group by eh.uri, eh.app")
    ViewStats getStatsByUri(String uri, Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStatsUniqueIp(eh.app, eh.uri, eh.ip) from EndpointHit as eh where " +
            "eh.uri = concat('', ?1, '') and eh.timestamp >= ?2 and eh.timestamp <= ?3 group by eh.ip, eh.uri, eh.app")
    List<ViewStatsUniqueIp> getStatsByUriWithUniqueIp(String uri, Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.uri)) from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <= ?2 group by eh.uri, eh.app order by count(eh.uri) desc")
    List<ViewStats> getStatsWithoutUri(Instant start, Instant end);

    @Query("select NEW ru.practicum.model.ViewStatsUniqueIp(eh.app, eh.uri, eh.ip) from EndpointHit as eh " +
            "where eh.timestamp >= ?1 and eh.timestamp <= ?2 group by eh.ip, eh.uri, eh.app")
    List<ViewStatsUniqueIp> getStatsWithoutUriWithUniqueIp(Instant start, Instant end);
}
