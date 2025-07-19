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

        UserDto userDto = UserMapper.toUserDto(user);
        log.debug("Пользователь {} успешно найден.", userDto);
        return userDto;
    }

    @Override
    public Collection<UserDto> findAll() {
        Collection<User> users = userRepository.findAll();
        List<UserDto> userList = users.stream()
                .map(UserMapper::toUserDto)
                .toList();

        log.debug("Получен список всех пользователей {}.", userList);
        return userList;
    }

    @Override
    public UserDto create(UserDto userDto) {
        mailExistCheck(userDto.getEmail());

        userDto = UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userDto)));

        log.debug("Пользователь {} создан.", userDto);
        return userDto;
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User updateUser = userExistCheck(userId);

        mailExistCheck(userDto.getEmail());

        if (userDto.getName() != null && !userDto.getName().isBlank()) updateUser.setName(userDto.getName());
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) updateUser.setEmail(userDto.getEmail());

        userDto = UserMapper.toUserDto(userRepository.save(updateUser));

        log.debug("Пользователь {} обновлен.", userDto);
        return userDto;
    }

    @Override
    public void delete(Long userId) {
        User deletedUser = userExistCheck(userId);

        log.debug("Пользователь {} удален.", deletedUser);
        userRepository.deleteById(userId);
    }

    private void mailExistCheck(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Пользователь с почтой {} уже существует.", email);
            throw new DuplicateEmailException("Email already exists");
        }
    }

    private User userExistCheck(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            log.error("Пользователь {} не найден!", id);
            throw new EntityNotFoundException("Пользователь", id);
        }

        return userRepository.findById(id).get();
    }
}