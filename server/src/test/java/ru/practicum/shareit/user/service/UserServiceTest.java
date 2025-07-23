package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final UserService userService;
    private UserDto userDto;
    private final UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(null, "John", "john@mail.com");

        User user = userRepository.save(UserMapper.toUser(userDto));

        userDto.setId(user.getId());
    }

    @Test
    void testFindById() {
        UserDto foundUser = userService.find(userDto.getId());
        assertNotNull(foundUser);
        assertEquals(userDto.getId(), foundUser.getId());
        assertEquals(userDto.getName(), foundUser.getName());
        assertEquals(userDto.getEmail(), foundUser.getEmail());
    }

    @Test
    void testFindById_NotFound() {
        assertThrows(EntityNotFoundException.class, () -> userService.find(999L));
    }

    @Test
    void testFindAll() {
        assertFalse(userService.findAll().isEmpty());
    }

    @Test
    void testFindAll_EmptyList() {
        userService.delete(userDto.getId());
        assertTrue(userService.findAll().isEmpty());
    }

    @Test
    void testSaveUser() {
        UserDto newUserDto = new UserDto(null, "Jane", "jane@mail.com");

        UserDto saveUser = userService.create(newUserDto);
        UserDto findUser = userService.find(saveUser.getId());
        assertNotNull(findUser);
        assertThat(findUser.getEmail(), equalTo(newUserDto.getEmail()));
        assertThat(findUser.getName(), equalTo(newUserDto.getName()));
        assertThat(findUser.getId(), equalTo(saveUser.getId()));
    }

    @Test
    void testSaveUser_DuplicateEmail() {
        UserDto duplicateUserDto = new UserDto(null, "Rick", "john@mail.com");

        assertThrows(DuplicateEmailException.class, () -> userService.create(duplicateUserDto));
    }

    @Test
    void testDeleteUser() {
        UserDto deleteUserDto = new UserDto(null, "Delete User", "delete@mail.com");
        UserDto deleteUser = userService.create(deleteUserDto);

        userService.delete(deleteUser.getId());

        assertFalse(userRepository.findById(deleteUser.getId()).isPresent());
    }

    @Test
    void testUpdateUser() {
        UserDto newUserDto = new UserDto(null, "Updated John", "updated@mail.com");

        UserDto user = userService.create(newUserDto);

        UserDto updatedUserDto = new UserDto(null, "Updated John", null);
        userService.update(updatedUserDto, user.getId());

        UserDto updatedUser = userService.find(user.getId());

        assertEquals(user.getId(), updatedUser.getId());
        assertEquals(updatedUserDto.getName(), updatedUser.getName());
        assertEquals(newUserDto.getEmail(), updatedUser.getEmail());
    }

    @Test
    void testSaveMultipleUsers() {
        UserDto userDto1 = new UserDto(null, "User One", "user1@example.com");
        UserDto userDto2 = new UserDto(null, "User Two", "user2@example.com");
        UserDto userDto3 = new UserDto(null, "User Three", "user3@example.com");

        userService.create(userDto1);
        userService.create(userDto2);
        userService.create(userDto3);


        Collection<UserDto> users = userService.findAll();
        assertEquals(4, users.size());

        assertTrue(users.stream().anyMatch(user -> user.getName().equals("User One")));
        assertTrue(users.stream().anyMatch(user -> user.getName().equals("User Two")));
        assertTrue(users.stream().anyMatch(user -> user.getName().equals("User Three")));
    }
}