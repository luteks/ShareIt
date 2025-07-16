package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto find(Long userId) {
        User user = userRepository.read(userId);
        if (user == null) throw new EntityNotFoundException("Пользователь", userId);
        return UserMapper.toUserDto(user);
    }

    public Collection<UserDto> findAll() {
        Collection<User> users = userRepository.readAll();
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    public UserDto create(UserDto userDto) {
        return UserMapper.toUserDto(userRepository.create(userDto));
    }

    public UserDto update(UserDto userDto, Long userId) {
        if (userRepository.read(userId) == null) {
            throw new EntityNotFoundException("Пользователь", userId);
        }
        return UserMapper.toUserDto(userRepository.update(userDto, userId));
    }

    public void delete(Long userId) {
        userRepository.delete(userId);
    }
}