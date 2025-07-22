package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    UserService userService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final UserDto userDto = UserDto.builder()
            .id(1L)
            .name("Michael")
            .email("michael@mail.com")
            .build();

    @Test
    void saveTest() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenReturn(userDto);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(status().isCreated());
    }

    @Test
    void updateTest() throws Exception {
        when(userService.update(any(), anyLong()))
                .thenReturn(userDto);
        mvc.perform(patch("/users/{userId}", 1)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.find(any()))
                .thenReturn(userDto);
        mvc.perform(get("/users/{userId}", 1)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUserByIdTest() throws Exception {
        mvc.perform(delete("/users/{userId}", 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void getAllTest() throws Exception {
        when(userService.findAll())
                .thenReturn(List.of(userDto));
        mvc.perform(get("/users")
                )
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void saveUserEmailExceptionTest() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenThrow(DuplicateEmailException.class);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void saveUserValidationExceptionTest() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserNotFoundExceptionTest() throws Exception {
        when(userService.find(any()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/users/{userId}", 7)
                )
                .andExpect(status().isNotFound());
    }
}