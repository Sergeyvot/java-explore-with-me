package ru.practicum.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByRequesterAndEvent(long requesterId, long eventId);

    List<Request> findByRequester(long requesterId);

    List<Request> findAllByEvent(long eventId);

    @Query("select r from Request as r where " +
            "r.id in (?1)")
    List<Request> findByRequesters(List<Long> requestIds);

    @Query("select r from Request as r where " +
            "r.event = ?1 and r.status = concat('', ?2, '')")
    List<Request> findAllByEventAndStatus(long eventId, String status);
}
