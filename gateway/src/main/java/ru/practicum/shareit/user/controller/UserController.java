package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.Marker;

@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.debug("Попытка вывода всех пользователей.");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUser(@NotNull @PathVariable Long userId) {
        log.debug("Попытка вывода пользователя {}.", userId);
        return userClient.getUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createUser(@RequestBody @Valid UserDto userDto) {
        log.debug("Попытка создания пользователя {}.", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated(Marker.OnUpdate.class)
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserDto userDto,
                                             @PathVariable Long userId) {
        log.debug("Попытка обновления пользователя {}.", userId);
        return userClient.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.debug("Попытка удаления пользователя {}.", userId);
        userClient.deleteUser(userId);
    }
}