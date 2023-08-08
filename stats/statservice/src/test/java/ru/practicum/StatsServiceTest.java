package ru.practicum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dao.EndpointHitRepository;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.ViewStats;
import ru.practicum.model.ViewStatsUniqueIp;
import ru.practicum.service.StatsService;
import ru.practicum.service.StatsServiceImpl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {
    @Mock
    EndpointHitRepository mockEndpointHitRepository;
    StatsService statsService;

    @BeforeEach
    @Test
    void initializingService() {
        statsService = new StatsServiceImpl(mockEndpointHitRepository);
    }

    @Test
    void testGetStatsWithUriWithoutUnique() {
        Instant start = Instant.now().minusSeconds(2000);
        Instant end = Instant.now().plusSeconds(2000);
        LocalDateTime startDateTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(end, ZoneId.systemDefault());
        Mockito
                .when(mockEndpointHitRepository.getStatsByUri("/events", start, end))
                .thenReturn(new ViewStats("ewm-main-service", "/events", 1L));
        Mockito
                .when(mockEndpointHitRepository.getStatsByUri("/events/1", start, end))
                .thenReturn(new ViewStats("ewm-main-service", "/events/1", 2L));

        List<ViewStatsDto> resultList = statsService.getStats(startDateTime, endDateTime,
                Arrays.asList("/events", "/events/1"), false);

        Assertions.assertEquals(2, resultList.size(), "Размер списка не совпадает");
        Assertions.assertEquals("ewm-main-service", resultList.get(0).getApp(),"Поля объектов не совпадают");
        Assertions.assertEquals("/events/1", resultList.get(0).getUri(),"Поля объектов не совпадают");
        Assertions.assertEquals(2L, resultList.get(0).getHits(),"Поля объектов не совпадают");
        Assertions.assertEquals("ewm-main-service", resultList.get(1).getApp(), "Поля объектов не совпадают");
        Assertions.assertEquals("/events", resultList.get(1).getUri(), "Поля объектов не совпадают");
        Assertions.assertEquals(1L, resultList.get(1).getHits(),"Поля объектов не совпадают");
    }

    @Test
    void testGetStatsWithIncorrectCheckTime() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusSeconds(1000);

        final ValidationException exception = Assertions.assertThrows(
                ValidationException.class,
                () -> statsService.getStats(start, end, Arrays.asList("/events", "/events/1"), false));

        Assertions.assertEquals("Время запроса задано некорректно.", exception.getMessage());

        LocalDateTime start2 = LocalDateTime.now();

        final ValidationException exception2 = Assertions.assertThrows(
                ValidationException.class,
                () -> statsService.getStats(start2, start2, Arrays.asList("/events", "/events/1"), false));

        Assertions.assertEquals("Время запроса задано некорректно.", exception2.getMessage());
    }

    @Test
    void testGetStatsWithUriWithUnique() {
        Instant start = Instant.now().minusSeconds(2000);
        Instant end = Instant.now().plusSeconds(2000);
        LocalDateTime startDateTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        Mockito
                .when(mockEndpointHitRepository.getStatsByUriWithUniqueIp("/events", start, end))
                .thenReturn(Arrays.asList(new ViewStatsUniqueIp("ewm-main-service", "/events", "192.163.0.1"),
                        new ViewStatsUniqueIp("ewm-main-service", "/events", "192.164.0.2")));
        Mockito
                .when(mockEndpointHitRepository.getStatsByUriWithUniqueIp("/events/1", start, end))
                .thenReturn(Arrays.asList(new ViewStatsUniqueIp("ewm-main-service", "/events/1", "192.164.0.2")));

        List<ViewStatsDto> resultList = statsService.getStats(startDateTime, endDateTime,
                Arrays.asList("/events", "/events/1"), true);

        Assertions.assertEquals(2, resultList.size(), "Размер списка не совпадает");
        Assertions.assertEquals("ewm-main-service", resultList.get(0).getApp(),"Поля объектов не совпадают");
        Assertions.assertEquals("/events", resultList.get(0).getUri(),"Поля объектов не совпадают");
        Assertions.assertEquals(2L, resultList.get(0).getHits(),"Поля объектов не совпадают");
        Assertions.assertEquals("ewm-main-service", resultList.get(1).getApp(), "Поля объектов не совпадают");
        Assertions.assertEquals("/events/1", resultList.get(1).getUri(), "Поля объектов не совпадают");
        Assertions.assertEquals(1L, resultList.get(1).getHits(),"Поля объектов не совпадают");
    }

    @Test
    void testGetStatsWithoutUriWithoutUnique() {
        Instant start = Instant.now().minusSeconds(2000);
        Instant end = Instant.now().plusSeconds(2000);
        LocalDateTime startDateTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        Mockito
                .when(mockEndpointHitRepository.getStatsWithoutUri(start, end))
                .thenReturn(Arrays.asList(new ViewStats("ewm-main-service", "/events", 3L),
                        new ViewStats("ewm-main-service", "/events/1", 1L)));

        List<ViewStatsDto> resultList = statsService.getStats(startDateTime, endDateTime,
                null, false);

        Assertions.assertEquals(2, resultList.size(), "Размер списка не совпадает");
        Assertions.assertEquals("ewm-main-service", resultList.get(0).getApp(),"Поля объектов не совпадают");
        Assertions.assertEquals("/events", resultList.get(0).getUri(),"Поля объектов не совпадают");
        Assertions.assertEquals(3L, resultList.get(0).getHits(),"Поля объектов не совпадают");
        Assertions.assertEquals("ewm-main-service", resultList.get(1).getApp(), "Поля объектов не совпадают");
        Assertions.assertEquals("/events/1", resultList.get(1).getUri(), "Поля объектов не совпадают");
        Assertions.assertEquals(1L, resultList.get(1).getHits(),"Поля объектов не совпадают");
    }

    @Test
    void testGetStatsWithoutUriWithUnique() {
        Instant start = Instant.now().minusSeconds(2000);
        Instant end = Instant.now().plusSeconds(2000);
        LocalDateTime startDateTime = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endDateTime = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        Mockito
                .when(mockEndpointHitRepository.getStatsWithoutUriWithUniqueIp(start, end))
                .thenReturn(Arrays.asList(new ViewStatsUniqueIp("ewm-main-service", "/events", "192.163.0.1"),
                        new ViewStatsUniqueIp("ewm-main-service", "/events/1", "192.164.0.1"),
                        new ViewStatsUniqueIp("ewm-main-service", "/events/1", "192.164.0.2")));

        List<ViewStatsDto> resultList = statsService.getStats(startDateTime, endDateTime,
                null, true);

        Assertions.assertEquals(2, resultList.size(), "Размер списка не совпадает");
        Assertions.assertEquals("ewm-main-service", resultList.get(0).getApp(),"Поля объектов не совпадают");
        Assertions.assertEquals("/events/1", resultList.get(0).getUri(),"Поля объектов не совпадают");
        Assertions.assertEquals(2L, resultList.get(0).getHits(),"Поля объектов не совпадают");
        Assertions.assertEquals("ewm-main-service", resultList.get(1).getApp(), "Поля объектов не совпадают");
        Assertions.assertEquals("/events", resultList.get(1).getUri(), "Поля объектов не совпадают");
        Assertions.assertEquals(1L, resultList.get(1).getHits(),"Поля объектов не совпадают");
    }
}
