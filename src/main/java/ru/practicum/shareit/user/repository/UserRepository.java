package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {

    Optional<User> read(Long userId);

    Collection<User> readAll();

    User create(User user);

    User update(User user, Long userId);

    void delete(Long userId);

    Boolean isEmailExist(String email);
}