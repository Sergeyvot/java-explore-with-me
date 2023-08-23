package ru.practicum.event_compilation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@IdClass(EventCompilationId.class)
@Table(name = "events_compilations", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class EventCompilation {
    @Id
    @Column(name = "event_id")
    private long eventId;
    @Id
    @Column(name = "compilation_id")
    private long compilationId;
}
