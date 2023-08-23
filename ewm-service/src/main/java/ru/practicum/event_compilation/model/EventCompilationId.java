package ru.practicum.event_compilation.model;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
public class EventCompilationId implements Serializable {
    private long eventId;
    private long compilationId;

    public EventCompilationId(long eventId, long compilationId) {
        this.eventId = eventId;
        this.compilationId = compilationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventCompilationId that = (EventCompilationId) o;
        return eventId == that.eventId && compilationId == that.compilationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, compilationId);
    }
}
