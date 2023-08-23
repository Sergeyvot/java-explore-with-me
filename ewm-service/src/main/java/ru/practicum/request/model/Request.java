package ru.practicum.request.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.request.Status;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "requests", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant created;
    @Column(name = "event_id")
    private long event;
    @Column(name = "requester_id")
    private long requester;
    @Enumerated(EnumType.STRING)
    private Status status;
}
