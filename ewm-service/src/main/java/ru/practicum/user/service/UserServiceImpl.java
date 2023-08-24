package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.UserMapperUtil;
import ru.practicum.user.dao.UserRepository;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User checkUser = repository.findUserByName(userDto.getName());
        if (checkUser != null) {
            throw new ConflictException("The user name must be unique");
        }

        User user = repository.save(UserMapperUtil.toUser(userDto));

        log.info("Добавлен пользователь с id {}", user.getId());
        return UserMapperUtil.toUserDto(user);
    }

    @Override
    public List<UserDto> findAllUsers(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<User> users = repository.findAll(pageable);
        log.info("Запрошен список всех пользователей приложения в границах пагинациию Данные получены");
        return users.stream()
                .map(UserMapperUtil::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findUsersById(List<Integer> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Long> longIds = ids.stream()
                .mapToLong(Integer::longValue)
                .boxed().collect(Collectors.toList());
        log.info("Запрошены пользователи с id из списка {}. Данные получены", ids);
        return repository.findUsersById(longIds, pageable)
                .stream()
                .map(UserMapperUtil::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeUser(long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        log.info("Из приложения удален пользователь с id {}.", userId);
        repository.deleteById(userId);
    }

    @Override
    public User findUserById(long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
        log.info("Запрошен пользователь с id {}. Данные получены", userId);
        return user;
    }
}
