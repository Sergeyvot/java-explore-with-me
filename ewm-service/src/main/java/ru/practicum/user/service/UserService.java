package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {

    UserDto createUser(UserDto userDto);

    List<UserDto> findAllUsers(Integer from, Integer size);

    List<UserDto> findUsersById(List<Integer> ids, Integer from, Integer size);

    void removeUser(long userId);

    User findUserById(long userId);
}
