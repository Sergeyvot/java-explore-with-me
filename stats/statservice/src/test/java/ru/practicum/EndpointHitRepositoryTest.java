package ru.practicum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
public class EndpointHitRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private EndpointHitRepository repository;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    void testGetStatsByUri() {
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(1000)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(500)));
        repository.save(makeEndpointHit("ewm-main-service", "/events",
                "192.163.0.1", Instant.now().minusSeconds(400)));

        TypedQuery<ViewStats> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.uri)) from EndpointHit as eh " +
                                "where eh.uri in (:uris) and eh.timestamp >= :start and eh.timestamp <= :end " +
                                "group by eh.uri, eh.app order by count(eh.uri) desc",
                        ViewStats.class);
        List<ViewStats> result = query.setParameter("uris", Arrays.asList("/events/1", "/events"))
                .setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getHits(), equalTo(2L));
        assertThat(result.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(1).getUri(), equalTo("/events"));
        assertThat(result.get(1).getHits(), equalTo(1L));
    }

    @Test
    void testGetStatsByUriWithUniqueIp() {
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(1000)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(500)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.164.0.2", Instant.now().minusSeconds(400)));

        TypedQuery<ViewStats> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) from EndpointHit as eh where " +
                        "eh.uri in (:uris) and eh.timestamp >= :start and eh.timestamp <= :end group by eh.uri, eh.app " +
                        "order by count(distinct eh.ip) desc", ViewStats.class);
        List<ViewStats> result = query.setParameter("uris", Arrays.asList("/events/1", "/events"))
                .setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getHits(), equalTo(2L));
    }

    @Test
    void testGetStatsWithoutUri() {
        repository.save(makeEndpointHit("ewm-main-service", "/events",
                "192.163.0.1", Instant.now().minusSeconds(1000)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(500)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.164.0.2", Instant.now().minusSeconds(400)));

        TypedQuery<ViewStats> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(eh.uri)) from EndpointHit as eh " +
                        "where eh.timestamp >= :start and eh.timestamp <= :end group by eh.uri, eh.app order by count(eh.uri) desc",
                        ViewStats.class);
        List<ViewStats> result = query.setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getHits(), equalTo(2L));
        assertThat(result.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(1).getUri(), equalTo("/events"));
        assertThat(result.get(1).getHits(), equalTo(1L));
    }

    @Test
    void testGetStatsWithoutUriWithUniqueIp() {
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(1000)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.165.0.3", Instant.now().minusSeconds(500)));
        repository.save(makeEndpointHit("ewm-main-service", "/events",
                "192.164.0.2", Instant.now().minusSeconds(400)));

        TypedQuery<ViewStats> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStats(eh.app, eh.uri, count(distinct eh.ip)) " +
                                "from EndpointHit as eh where eh.timestamp >= :start and eh.timestamp <= :end group by eh.uri, " +
                                "eh.app order by count(distinct eh.ip) desc", ViewStats.class);
        List<ViewStats> result = query.setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getHits(), equalTo(2L));
        assertThat(result.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(1).getUri(), equalTo("/events"));
        assertThat(result.get(1).getHits(), equalTo(1L));
    }

    private EndpointHit makeEndpointHit(String app, String uri, String ip, Instant timestamp) {

        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp).build();
    }
}
