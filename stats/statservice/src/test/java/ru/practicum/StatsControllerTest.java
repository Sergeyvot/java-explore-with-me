package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.controller.StatsController;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {
    @MockBean
    StatsService statsService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final HitDto hitDto = new HitDto(
            1L,
            "ewm-main-service",
            "/events/1",
            "172.16.13.98 /24", LocalDateTime.of(2023, 8, 25, 12, 0, 0));

    private final List<ViewStatsDto> list = Stream.of(new ViewStatsDto("ewm-main-service", "/events", 3L),
            new ViewStatsDto("ewm-main-service", "/events/1", 1L)).collect(Collectors.toList());

    @Test
    void testAddHit() throws Exception {

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hitDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(statsService, Mockito.times(1))
                .addPost(any());

        Mockito.verifyNoMoreInteractions(statsService);
    }

    @Test
    void testGetStats() throws Exception {
        when(statsService.getStats(any(), any(), anyList(), anyBoolean()))
                .thenReturn(list);

        mvc.perform(get("/stats?start=2022-11-20 12:00:00&end=2023-11-20 12:00:00&uris=[/events]")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].app", is(list.get(0).getApp())))
                .andExpect(jsonPath("$[0].uri", is(list.get(0).getUri())))
                .andExpect(jsonPath("$[0].hits", is(list.get(0).getHits()), Long.class))
                .andExpect(jsonPath("$[1].app", is(list.get(1).getApp())))
                .andExpect(jsonPath("$[1].uri", is(list.get(1).getUri())))
                .andExpect(jsonPath("$[1].hits", is(list.get(1).getHits()), Long.class));
    }
}
