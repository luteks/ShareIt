package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@AllArgsConstructor
public class UserRepositoryInMemory implements UserRepository {
    private final HashMap<Long, User> users = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();

    @Override
    public Optional<User> read(Long userId) {
        return Optional.of(users.get(userId));
    }

    @Override
    public Collection<User> readAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user, Long userId) {
        String oldEmail = users.get(userId).getEmail();
        if (user.getEmail() != null) {
            if (!oldEmail.equals(user.getEmail())) {
                emailSet.remove(oldEmail);
            }
        }

        users.put(userId, user);
        return user;
    }

    @Override
    public void delete(Long userId) {
        User removedUser = users.remove(userId);
        emailSet.remove(removedUser.getEmail());
    }

    private Long getNextId() {
        return users.isEmpty() ? 1 : Collections.max(users.keySet()) + 1;
    }

    @Override
    public Boolean isEmailExist(String email) {
        return emailSet.contains(email);
    }
}