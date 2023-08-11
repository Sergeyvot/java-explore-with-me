package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.ViewStatsDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class ClientController {
    private final StatClient statClient;

    @PostMapping("/hit")
    public void addHit(@RequestBody @Valid HitDto hitDto) {

        statClient.addPost(hitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getBookings(@RequestParam(name = "start") @NotNull String start,
                                          @RequestParam(name = "end") @NotNull String end,
                                          @RequestParam(name = "uris", required = false) List<String> uris,
                                          @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {

        return statClient.getStats(start, end, uris, unique);
    }
}
