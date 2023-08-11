package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.ViewStats;
import ru.practicum.model.ViewStatsUniqueIp;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceImplTest {

    private final StatsService statsService;
    private final ViewStats viewStats;
    private final ViewStatsUniqueIp viewStatsUniqueIp;

    @Test
    void testGetStats() {

        List<HitDto> sourceHits = Stream.of(
                makeHitDto("ewm-main-service", "/events/1", "192.164.0.1",
                        LocalDateTime.of(2023, 8, 10, 12, 0, 0)),
                makeHitDto("ewm-main-service", "/events/1", "192.164.0.2",
                        LocalDateTime.of(2023, 8, 11, 12, 0, 0)),
                makeHitDto("ewm-main-service", "/events", "192.164.0.1",
                        LocalDateTime.of(2023, 8, 10, 13, 0, 0)),
                makeHitDto("ewm-main-service", "/events", "192.164.0.1",
                        LocalDateTime.of(2023, 8, 11, 13, 0, 0))
        ).collect(Collectors.toList());

        for (HitDto hitDto : sourceHits) {
            statsService.addPost(hitDto);
        }

        List<ViewStatsDto> list = statsService.getStats(LocalDateTime.now().minusDays(60), LocalDateTime.now().plusDays(60),
                Arrays.asList("/events/1", "/events"), true);

        assertThat(list, hasSize(2));
        assertThat(list.get(0).getApp(), equalTo("ewm-main-service"));
        assertThat(list.get(0).getUri(), equalTo("/events/1"));
        assertThat(list.get(0).getHits(), equalTo(2L));
        assertThat(list.get(1).getApp(), equalTo("ewm-main-service"));
        assertThat(list.get(1).getUri(), equalTo("/events"));
        assertThat(list.get(1).getHits(), equalTo(1L));
    }

    private HitDto makeHitDto(String app, String uri, String ip, LocalDateTime timestamp) {

        return HitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp).build();
    }
}
