package ru.practicum.user.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where u.id in (?1)")
    Page<User> findUsersById(List<Long> ids, Pageable pageable);

    Page<User> findAll(Pageable pageable);

    User findUserByName(String name);
}
