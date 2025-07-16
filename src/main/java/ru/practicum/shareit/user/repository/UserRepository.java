package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    User read(Long userId);

    Collection<User> readAll();

    User create(UserDto userDto);

    User update(UserDto userDto, Long userId);

    void delete(Long userId);
}