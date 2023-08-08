package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
        /*BookingDtoView result = bookingService.addNewBooking(userId, bookingDto);
        if (result != null) {
            log.info("Запрос выполнен. В приложение добавлено бронирование вещи с id {}", result.getItem().getId());
        } else {
            log.info("Бронирование вещи с id {} не удалось", bookingDto.getItemId());
        }
        return result;*/
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam(name = "start") @NotNull String start,
                                       @RequestParam(name = "end") @NotNull String end,
                                       @RequestParam(name = "uris", required = false) List<String> uris,
                                       @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);
        /*BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + stateParam));
        checkPaginationParamsGateway(from, size);
        log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsOwner(userId, state, from, size);*/
        return statsService.getStats(startDateTime, endDateTime, uris, unique);
    }
}
