package ru.practicum.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, EventOptionalRepository {

    List<Event> findAllByCategoryId(long categoryId);

    Page<Event> findAllByInitiatorId(long userId, Pageable pageable);

    @Query("select ev from Event as ev where " +
            "ev.id = ?1 and ev.state = concat('', ?2, '')")
    Event findByIdAndStatePublished(long eventId, String state);
}
