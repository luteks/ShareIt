package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
        userDto = UserDto.builder()
                .id(1L)
                .name("Paul")
                .email("paul@mail.com")
                .build();
        user = UserMapper.toUser(userDto);
        user.setId(userDto.getId());
    }

    @Test
    void testFindById_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto foundUser = userService.find(user.getId());

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getEmail(), foundUser.getEmail());
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.find(user.getId()));
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        Collection<UserDto> users = userService.findAll();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void testFindAll_EmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        Collection<UserDto> users = userService.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void testSaveUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto savedUser = userService.create(userDto);

        assertNotNull(savedUser);
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto updatedUserDto = new UserDto(user.getId(), "Updated John", "updated.john@example.com");

        UserDto updatedUser = userService.update(updatedUserDto, user.getId());

        assertEquals(updatedUserDto.getName(), updatedUser.getName());
        assertEquals(updatedUserDto.getEmail(), updatedUser.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testSaveUser_DuplicateEmail() {
        when(userRepository.save(any(User.class))).thenThrow(new DuplicateEmailException("Email already exists"));

        assertThrows(DuplicateEmailException.class, () -> userService.create(userDto));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser_Success() {
        userService.delete(user.getId());

        verify(userRepository, times(1)).deleteById(user.getId());
    }
}