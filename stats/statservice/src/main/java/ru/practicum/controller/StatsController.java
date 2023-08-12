package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsService;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {

    private final StatsService statsService;
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    public void addHit(@RequestBody HitDto hitDto) {

        statsService.addPost(hitDto);

        log.info("Save hit {}", hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(name = "start") @NotNull String start,
                                       @RequestParam(name = "end") @NotNull String end,
                                       @RequestParam(name = "uris", required = false) List<String> uris,
                                       @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        List<ViewStatsDto> resultList = statsService.getStats(startDateTime, endDateTime, uris, unique);
        if (resultList != null) {
            log.info("Get stats with start {}, end={}, uris={}, unique={}", start, end, uris, unique);
        } else {
            log.info("Get stats not executed");
        }
        return resultList;
    }
}
