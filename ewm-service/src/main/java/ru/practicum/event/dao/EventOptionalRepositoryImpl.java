package ru.practicum.event.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.model.Category;
import ru.practicum.event.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.RequestParamAdmin;
import ru.practicum.event.model.RequestParamUser;
import ru.practicum.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventOptionalRepositoryImpl implements EventOptionalRepository{

    private final EntityManager entityManager;
    private final CriteriaBuilder cb;

    public EventOptionalRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.cb = entityManager.getCriteriaBuilder();
    }

    @Override
    public Page<Event> findEventsByParametersUser(Pageable pageable, RequestParamUser parameters) {
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = getPredicateBuUserParameters(parameters, eventRoot);
        criteriaQuery.where(predicate);

        if (parameters.getSort() != null && parameters.getSort().equals("EVENT_DATE")) {
            criteriaQuery.orderBy(cb.asc(eventRoot.get("eventDate")));
        }
        if (parameters.getSort() != null && parameters.getSort().equals("VIEWS")) {
            criteriaQuery.orderBy(cb.desc(eventRoot.get("views")));
        }

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> events = typedQuery.getResultList();
        return new PageImpl<>(events);
    }

    @Override
    public Page<Event> findEventsByParametersAdmin(Pageable pageable, RequestParamAdmin parameters) {
        CriteriaQuery<Event> criteriaQuery = cb.createQuery(Event.class);
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        Predicate predicate = getPredicateBuAdminParameters(parameters, eventRoot);
        criteriaQuery.where(predicate);

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> events = typedQuery.getResultList();
        return new PageImpl<>(events);
    }

    private Predicate getPredicateBuUserParameters(RequestParamUser parameters, Root<Event> eventRoot) {
        Instant rangeStartInstant;
        Instant rangeEndInstant;
        if (parameters.getRangeStart() != null && parameters.getRangeEnd() != null) {
            rangeStartInstant = parameters.getRangeStart().toInstant(OffsetDateTime.now().getOffset());
            rangeEndInstant = parameters.getRangeEnd().toInstant(OffsetDateTime.now().getOffset());
        } else {
            rangeStartInstant = LocalDateTime.now().toInstant(OffsetDateTime.now().getOffset());
            rangeEndInstant = LocalDateTime.now().plusYears(10).toInstant(OffsetDateTime.now().getOffset());
        }

        List<Predicate> predicates = new ArrayList<>();

        if (parameters.getText() != null) {
            Predicate containsTextAnnotation = cb.like(cb.upper(eventRoot.get("annotation")),
                    "%" + parameters.getText().toUpperCase() + "%");
            Predicate containsTextDescription = cb.like(cb.upper(eventRoot.get("description")),
                    "%" + parameters.getText().toUpperCase() + "%");
            predicates.add(cb.or(containsTextAnnotation, containsTextDescription));
        }
        if (parameters.getCategories() != null && !parameters.getCategories().isEmpty()) {
            List<Long> categoriesLong = parameters.getCategories().stream()
                    .map(Long::valueOf).collect(Collectors.toList());
            Join<Event, Category> categoryJoin = eventRoot.join("category");
            predicates.add(categoryJoin.get("id").in(categoriesLong));
        }
        if (parameters.getPaid() != null) {
            predicates.add(cb.equal(eventRoot.get("paid"), parameters.getPaid()));
        }
        if (parameters.getOnlyAvailable()) {
            predicates.add(cb.lt(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit")));
        }
        predicates.add(cb.between(eventRoot.get("eventDate"), rangeStartInstant, rangeEndInstant));

        return cb.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }

    private Predicate getPredicateBuAdminParameters(RequestParamAdmin parameters, Root<Event> eventRoot) {
        Instant rangeStartInstant;
        Instant rangeEndInstant;
        if (parameters.getRangeStart() != null && parameters.getRangeEnd() != null) {
            rangeStartInstant = parameters.getRangeStart().toInstant(OffsetDateTime.now().getOffset());
            rangeEndInstant = parameters.getRangeEnd().toInstant(OffsetDateTime.now().getOffset());
        } else {
            rangeStartInstant = LocalDateTime.now().minusYears(10).toInstant(OffsetDateTime.now().getOffset());
            rangeEndInstant = LocalDateTime.now().plusYears(10).toInstant(OffsetDateTime.now().getOffset());
        }

        List<Predicate> predicates = new ArrayList<>();

        if (parameters.getUsers() != null && !parameters.getUsers().isEmpty()) {
            List<Long> usersLong = parameters.getUsers().stream()
                    .map(Long::valueOf).collect(Collectors.toList());
            Join<Event, User> userJoin = eventRoot.join("initiator");
            predicates.add(userJoin.get("id").in(usersLong));
        }
        if (parameters.getStates() != null && !parameters.getStates().isEmpty()) {
            List<State> statesEnum = parameters.getStates().stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());
            predicates.add(eventRoot.get("state").in(statesEnum));
        }
        if (parameters.getCategories() != null && !parameters.getCategories().isEmpty()) {
            List<Long> categoriesLong = parameters.getCategories().stream()
                    .map(Long::valueOf).collect(Collectors.toList());
            Join<Event, Category> categoryJoin = eventRoot.join("category");
            predicates.add(categoryJoin.get("id").in(categoriesLong));
        }
        predicates.add(cb.between(eventRoot.get("eventDate"), rangeStartInstant, rangeEndInstant));

        return cb.and(predicates.toArray(predicates.toArray(new Predicate[0])));
    }
}
