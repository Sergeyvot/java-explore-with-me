package ru.practicum.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.event.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment as c where " +
            "c.event.id = ?1 and c.stateComment = concat('', ?2, '') order by c.publishedOn desc")
    Page<Comment> findAllByEventIdAndStatePublished(long eventId, String stateComment, Pageable pageable);

    @Query("select c from Comment as c where " +
            "c.event.id = ?1 order by c.publishedOn desc")
    Page<Comment> findAllByEventId(long eventId, Pageable pageable);

}
