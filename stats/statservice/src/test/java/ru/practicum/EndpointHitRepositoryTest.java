package ru.practicum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;
import ru.practicum.model.ViewStatsUniqueIp;

import javax.persistence.TypedQuery;
import java.time.Instant;
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
                        "where eh.uri = concat('', :uri, '') and eh.timestamp >= :start and eh.timestamp <= :end group by eh.uri, eh.app",
                        ViewStats.class);
        ViewStats result = query.setParameter("uri", "/events/1")
                .setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getSingleResult();

        assertThat(result.getApp(), equalTo("ewm-main-service"));
        assertThat(result.getUri(), equalTo("/events/1"));
        assertThat(result.getHits(), equalTo(2L));
    }

    @Test
    void testGetStatsByUriWithUniqueIp() {
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(1000)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.163.0.1", Instant.now().minusSeconds(500)));
        repository.save(makeEndpointHit("ewm-main-service", "/events/1",
                "192.164.0.2", Instant.now().minusSeconds(400)));

        TypedQuery<ViewStatsUniqueIp> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStatsUniqueIp(eh.app, eh.uri, eh.ip) from " +
                        "EndpointHit as eh where eh.uri = concat('', :uri, '') and eh.timestamp >= :start and " +
                        "eh.timestamp <= :end group by eh.ip, eh.uri, eh.app", ViewStatsUniqueIp.class);
        List<ViewStatsUniqueIp> result = query.setParameter("uri", "/events/1")
                .setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getIp(), equalTo("192.163.0.1"));
        assertThat(result.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(1).getUri(), equalTo("/events/1"));
        assertThat(result.get(1).getIp(), equalTo("192.164.0.2"));
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

        TypedQuery<ViewStatsUniqueIp> query = em.getEntityManager()
                .createQuery("select NEW ru.practicum.model.ViewStatsUniqueIp(eh.app, eh.uri, eh.ip) from EndpointHit as eh " +
                        "where eh.timestamp >= :start and eh.timestamp <= :end group by eh.ip, eh.uri, eh.app", ViewStatsUniqueIp.class);
        List<ViewStatsUniqueIp> result = query.setParameter("start", Instant.now().minusSeconds(2000))
                .setParameter("end", Instant.now().plusSeconds(2000)).getResultList();

        assertThat(result, hasSize(3));
        assertThat(result.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(0).getUri(), equalTo("/events/1"));
        assertThat(result.get(0).getIp(), equalTo("192.163.0.1"));
        assertThat(result.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(1).getUri(), equalTo("/events"));
        assertThat(result.get(1).getIp(), equalTo("192.164.0.2"));
        assertThat(result.get(2).getApp(), equalTo("ewm-main-service"));
        assertThat(result.get(2).getUri(), equalTo("/events/1"));
        assertThat(result.get(2).getIp(), equalTo("192.165.0.3"));
    }

    private EndpointHit makeEndpointHit(String app, String uri, String ip, Instant timestamp) {

        return EndpointHit.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp).build();
    }
}
