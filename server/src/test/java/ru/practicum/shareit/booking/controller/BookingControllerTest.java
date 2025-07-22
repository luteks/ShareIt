package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private final ItemDto itemDto = new ItemDto(1L, "Pen", "Blue pen", true, 1L);
    private final LocalDateTime startTime = LocalDateTime.of(2025, 6, 11, 10, 0);
    private final LocalDateTime endTime = startTime.plusDays(1);
    private final UserDto userDto = new UserDto(1L, "Lora", "lora@mail.com");
    private final String headerSharerUserId = "X-Sharer-User-Id";
    @MockBean
    BookingService bookingService;
    @MockBean
    ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .item(Item.builder()
                    .id(itemDto.getId())
                    .name(itemDto.getName())
                    .build())
            .booker(User.builder()
                    .id(userDto.getId())
                    .build())
            .status(BookingStatus.WAITING)
            .build();

    private final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
            .id(1L)
            .start(startTime)
            .end(endTime)
            .itemId(1L)
            .build();

    @Test
    void getAllBookingsTest() throws Exception {
        when(bookingService.findAllUserBookings(anyLong(), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "ALL")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker", notNullValue()))
                .andExpect(jsonPath("$[0].item", notNullValue()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsByOwnerIdTest() throws Exception {
        when(bookingService.findAllOwnerBookings(anyLong(), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));
        mvc.perform(get("/bookings/owner")
                        .header(headerSharerUserId, 1)
                        .param("state", "ALL")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$[0].booker", notNullValue()))
                .andExpect(jsonPath("$[0].item", notNullValue()))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void saveTest() throws Exception {
        when(bookingService.create(any(), any()))
                .thenReturn(bookingDto);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void approveTest() throws Exception {
        bookingDto.setStatus(BookingStatus.APPROVED);
        when(bookingService.update(anyLong(), anyLong(), eq(true)))
                .thenReturn(bookingDto);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByIdTest() throws Exception {
        when(bookingService.find(anyLong(), anyLong()))
                .thenReturn(bookingDto);
        mvc.perform(get("/bookings/{bookingId}", 1)
                        .header(headerSharerUserId, 1))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.booker", notNullValue()))
                .andExpect(jsonPath("$.item", notNullValue()))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsValidationUserExceptionTest() throws Exception {
        when(bookingService.findAllUserBookings(anyLong(), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "ALL")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingsByOwnerIdValidationExceptionTest() throws Exception {
        when(bookingService.findAllOwnerBookings(anyLong(), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/bookings/owner")
                        .header(headerSharerUserId, 1)
                        .param("state", "ALL")
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void saveNotFoundExceptionTest() throws Exception {
        when(bookingService.create(anyLong(), any()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }


    @Test
    void approveValidationExceptionTest() throws Exception {
        when(bookingService.update(anyLong(), anyLong(), eq(true)))
                .thenThrow(AccessDeniedException.class);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookingByIdNotFoundExceptionTest() throws Exception {
        when(bookingService.find(anyLong(), anyLong()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/bookings/{bookingId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void approveNotFoundExceptionTest() throws Exception {
        when(bookingService.update(anyLong(), anyLong(), eq(true)))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .param("approved", "true")
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookingOnItemUnavailable() throws Exception {
        when(bookingService.create(any(), any()))
                .thenThrow(ItemUnavailableException.class);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllBookingsInternalServerErrorTest() throws Exception {
        when(bookingService.findAllUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Внутренняя ошибка сервера"));
        mvc.perform(get("/bookings")
                        .header(headerSharerUserId, 1)
                        .param("state", "ALL")
                        .param("size", "10")
                        .param("from", "0")
                )
                .andExpect(status().isInternalServerError());
    }
}