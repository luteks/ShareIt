package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto find(Long userId) {
        log.debug("Начало поиска пользователя {}.", userId);
        userExistCheck(userId);
        User user = userRepository.read(userId);

        log.debug("Пользователь {} успешно найден.", user);
        return UserMapper.toUserDto(user);
    }

    public Collection<UserDto> findAll() {
        Collection<User> users = userRepository.readAll();
        List<UserDto> userList = users.stream()
                .map(UserMapper::toUserDto)
                .toList();

        log.debug("Получен список всех пользователей {}.", userList);
        return userList;
    }

    public UserDto create(UserDto userDto) {
        mailExistCheck(userDto.getEmail());

        User user = UserMapper.toUser(userDto, 0L);

        log.debug("Пользователь {} создан.", user);
        return UserMapper.toUserDto(userRepository.create(user));
    }

    public UserDto update(UserDto userDto, Long userId) {
        userExistCheck(userId);

        User user = userRepository.read(userId);
        String oldEmail = user.getEmail();
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) {
            if (!oldEmail.equals(userDto.getEmail())) {
                mailExistCheck(userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }

        log.debug("Пользователь {} обновлен.", userId);
        return UserMapper.toUserDto(userRepository.update(user, userId));
    }

    public void delete(Long userId) {
        log.debug("Пользователь {} удален.", userId);
        userRepository.delete(userId);
    }

    private void userExistCheck(Long id) {
        if (userRepository.read(id) == null) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new EntityNotFoundException("Пользователь", id);
        }
    }

    private void mailExistCheck(String email) {
        if (!userRepository.isEmailExist(email)){
            log.warn("Пользователь с почтой {} уже существует.", email);
            throw new DuplicateEmailException("Email already exists");
        }
    }
}