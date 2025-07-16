package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@AllArgsConstructor
public class UserRepositoryInMemory implements UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();

    @Override
    public User read(Long userId) {
        return users.get(userId);
    }

    @Override
    public Collection<User> readAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(UserDto userDto) {
        if (!emailSet.add(userDto.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }
        User user = UserMapper.toUser(userDto, getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(UserDto userDto, Long userId) {
        User updateUser = users.get(userId);
        String oldEmail = updateUser.getEmail();
        if (userDto.getName() != null) updateUser.setName(userDto.getName());
        if (userDto.getEmail() != null) {
            if (!oldEmail.equals(userDto.getEmail())) {
                emailSet.remove(oldEmail);
                if (!emailSet.add(userDto.getEmail())) {
                    throw new DuplicateEmailException("Email already exists");
                }
            }
            updateUser.setEmail(userDto.getEmail());
        }
        users.put(userId, updateUser);
        return updateUser;
    }

    @Override
    public void delete(Long userId) {
        User removedUser = users.remove(userId);
        emailSet.remove(removedUser.getEmail());
    }

    private Long getNextId() {
        return users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1;
    }
}