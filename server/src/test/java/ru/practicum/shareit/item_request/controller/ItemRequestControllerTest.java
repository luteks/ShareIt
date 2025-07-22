package ru.practicum.shareit.item_request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item_request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.item_request.dto.ItemRequestDto;
import ru.practicum.shareit.item_request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    private final LocalDateTime time = of(2025, 6, 11, 10, 0);
    private final String headerSharerUserId = "X-Sharer-User-Id";
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("I need this pen")
            .created(time)
            .build();

    private final ItemRequestAllFieldsDto itemRequestAllFieldsDto = ItemRequestAllFieldsDto.builder()
            .id(1L)
            .description("I need this pen")
            .created(time)
            .items(Collections.emptyList())
            .build();
    String expected = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    void createItemRequest() throws Exception {
        when(itemRequestService.create(any(), anyLong()))
                .thenReturn(itemRequestDto);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(jsonPath("$.created", is(expected)))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemRequests() throws Exception {
        when(itemRequestService.findAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestDto));
        mvc.perform(get("/requests/all")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].created", is(expected)))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getAllItemRequestsById() throws Exception {
        when(itemRequestService.findAllUserRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestAllFieldsDto));
        mvc.perform(get("/requests")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].created", is(expected)))
                .andExpect(jsonPath("$[0].description", is(itemRequestAllFieldsDto.getDescription())))
                .andExpect(jsonPath("$.[0].items", hasSize(0)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemRequestById() throws Exception {
        when(itemRequestService.find(anyLong(), anyLong()))
                .thenReturn(itemRequestAllFieldsDto);
        mvc.perform(get("/requests/{requestId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(jsonPath("$.created", is(expected)))
                .andExpect(jsonPath("$.description", is(itemRequestAllFieldsDto.getDescription())))
                .andExpect(jsonPath("$.id", is(itemRequestAllFieldsDto.getId()), Long.class))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    void getItemRequestNotFoundExceptionTest() throws Exception {
        when(itemRequestService.find(anyLong(), anyLong()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/requests/{requestId}", 1)
                        .header(headerSharerUserId, 27))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemRequestsWithNotFoundException() throws Exception {
        when(itemRequestService.findAllUserRequests(anyLong(), anyInt(), anyInt()))
                .thenThrow(EntityNotFoundException.class);

        mvc.perform(get("/requests")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0"))

                .andExpect(status().isNotFound());
    }

    @Test
    void saveItemRequestValidationExceptionTest() throws Exception {
        when(itemRequestService.create(any(), anyLong()))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/requests")
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}