package ru.practicum.event.model;

import lombok.*;
import ru.practicum.event.StateComment;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    @ManyToOne
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;
    @ManyToOne
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;
    @Column(name = "state_comment")
    @Enumerated(EnumType.STRING)
    private StateComment stateComment;
    @Column(name = "published_on")
    private Instant publishedOn;
}
