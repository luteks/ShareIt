package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.validation.Marker;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> getAll() {
        log.debug("Попытка вывода всех пользователей.");
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable Long userId) {
        log.debug("Попытка вывода пользователя {}.", userId);
        return userService.find(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Marker.OnCreate.class)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.debug("Попытка создания пользователя {}.", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    @Validated(Marker.OnUpdate.class)
    public UserDto update(@Valid @RequestBody UserDto updateUserDto, @PathVariable Long userId) {
        log.debug("Попытка обновления пользователя {}.", userId);
        return userService.update(updateUserDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.debug("Попытка удаления пользователя {}.", userId);
        userService.delete(userId);
    }
}