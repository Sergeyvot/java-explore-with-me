package ru.practicum.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "stats", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String app;
    @NotNull
    private String uri;
    @Column(name = "ip_address")
    @NotNull
    private String ip;
    @Column(name = "timestamp_date")
    @NotNull
    private Instant timestamp;
}
