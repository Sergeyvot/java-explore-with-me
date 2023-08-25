package ru.practicum.user;

import lombok.experimental.UtilityClass;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@UtilityClass
public final class UserMapperUtil {

    public User toUser(UserDto userDto) {
        User.UserBuilder user = User.builder();

        if (userDto.getId() != null) {
            user.id(userDto.getId());
        }
        user.name(userDto.getName());
        user.email(userDto.getEmail());

        return user.build();
    }

    public UserDto toUserDto(User user) {
        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id(user.getId());
        userDto.name(user.getName());
        userDto.email(user.getEmail());

        return userDto.build();
    }

    public UserShortDto toUserShortDto(User user) {
        UserShortDto.UserShortDtoBuilder userShortDto = UserShortDto.builder();

        userShortDto.id(user.getId());
        userShortDto.name(user.getName());

        return userShortDto.build();
    }
}
