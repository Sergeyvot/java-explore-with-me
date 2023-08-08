package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ViewStatsDto implements Comparable<ViewStatsDto>{
    @NotNull
    private String app;
    @NotNull
    private String uri;
    private Long hits;

    @Override
    public int compareTo(ViewStatsDto o) {
        return (int) (o.getHits() - this.hits);
    }
}
