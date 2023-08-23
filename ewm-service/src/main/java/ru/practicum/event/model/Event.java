package ru.practicum.event.model;

import lombok.*;
import ru.practicum.category.model.Category;
import ru.practicum.event.State;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "events", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;
    @ManyToOne
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;
    @Column(name = "confirmed_requests")
    private Integer confirmedRequests;
    @Column(name = "created_on")
    private Instant createdOn;
    private String description;
    @Column(name = "event_date")
    private Instant eventDate;
    @ManyToOne
    @JoinColumn(name = "initiator_id")
    @ToString.Exclude
    private User initiator;
    private float lat;
    private float lon;
    private Boolean paid;
    @Column(name = "participant_limit")
    private Integer participantLimit;
    @Column(name = "published_on")
    private Instant publishedOn;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    private State state;
    private String title;
    private Integer views;
}
