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
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto find(Long userId) {
        User user = userExistCheck(userId);

        log.debug("Пользователь {} успешно найден.", user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> findAll() {
        Collection<User> users = userRepository.readAll();
        List<UserDto> userList = users.stream()
                .map(UserMapper::toUserDto)
                .toList();

        log.debug("Получен список всех пользователей {}.", userList);
        return userList;
    }

    @Override
    public UserDto create(UserDto userDto) {
        mailExistCheck(userDto.getEmail());

        User user = UserMapper.toUser(userDto, 0L);

        log.debug("Пользователь {} создан.", user);
        return UserMapper.toUserDto(userRepository.create(user));
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User user = userExistCheck(userId);
        String oldEmail = user.getEmail();

        if (userDto.getName() != null && !userDto.getName().isBlank()) user.setName(userDto.getName());
        if (userDto.getEmail() != null) {
            if (!oldEmail.equals(userDto.getEmail()) && !userDto.getEmail().isBlank()) {
                mailExistCheck(userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }

        log.debug("Пользователь {} обновлен.", userId);
        return UserMapper.toUserDto(userRepository.update(user, userId));
    }

    @Override
    public void delete(Long userId) {
        userExistCheck(userId);

        log.debug("Пользователь {} удален.", userId);
        userRepository.delete(userId);
    }

    private User userExistCheck(Long id) {
        if (userRepository.read(id).isEmpty()) {
            log.warn("Пользователь с id = {} не найден", id);
            throw new EntityNotFoundException("Пользователь", id);
        }

        return userRepository.read(id).get();
    }

    private void mailExistCheck(String email) {
        if (!userRepository.isEmailExist(email)) {
            log.warn("Пользователь с почтой {} уже существует.", email);
            throw new DuplicateEmailException("Email already exists");
        }
    }
}