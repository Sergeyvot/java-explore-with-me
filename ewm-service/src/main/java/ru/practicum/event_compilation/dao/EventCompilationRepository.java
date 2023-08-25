package ru.practicum.event_compilation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event_compilation.model.EventCompilation;
import ru.practicum.event_compilation.model.EventId;

import java.util.List;

public interface EventCompilationRepository extends JpaRepository<EventCompilation, Long> {

    @Query("select NEW ru.practicum.event_compilation.model.EventId(evc.eventId) from EventCompilation as evc " +
            "where evc.compilationId = ?1")
    List<EventId> findAllEventIdByCompilationId(long compId);

    void deleteByCompilationId(long compId);
}
