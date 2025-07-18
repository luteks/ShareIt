package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto find(Long userId);

    Collection<UserDto> findAll();

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto, Long userId);

    void delete(Long userId);
}
