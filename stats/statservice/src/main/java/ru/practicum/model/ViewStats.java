package ru.practicum.model;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
public class ViewStats {
    private String app;
    private String uri;
    private Long hits;

    public ViewStats(String app, String uri, Long hits) {
        this.app = app;
        this.uri = uri;
        this.hits = hits;
    }
}
