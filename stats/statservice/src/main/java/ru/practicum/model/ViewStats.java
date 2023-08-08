package ru.practicum.model;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Component
@Data
@NoArgsConstructor
public class ViewStats {
    @NotNull
    private String app;
    @NotNull
    private String uri;
    private Long hits;


    public ViewStats(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;

    }
}
