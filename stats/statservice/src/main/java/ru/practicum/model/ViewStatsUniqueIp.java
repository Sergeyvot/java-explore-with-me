package ru.practicum.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Component
@Data
@NoArgsConstructor
public class ViewStatsUniqueIp {
    @NotNull
    private String app;
    @NotNull
    private String uri;
    private String ip;

    public ViewStatsUniqueIp(String app, String uri, String ip) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
    }
}
