package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.Marker;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@Validated
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public BookingDto create(@Valid @RequestBody BookingCreateDto bookingCreateDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка пользователем {} создания запроса бронирования {}.", userId, bookingCreateDto);
        return bookingService.create(bookingCreateDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @PathVariable("bookingId") Long bookingId,
                             @RequestParam(name = "approved") boolean approved) {
        log.debug("Попытка пользователем {} подтверждения запроса бронирования {}.", userId, bookingId);
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable("bookingId") Long bookingId) {
        log.debug("Попытка пользователем {} получения информации о бронировании {}.", userId, bookingId);
        return bookingService.find(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingDto> getAllUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam(name = "state", required = false,
                                                             defaultValue = "ALL") BookingState state) {
        log.debug("Попытка пользователем {} получения информации о всех бронированиях.", userId);
        return bookingService.findAllUserBookings(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(name = "state", required = false,
                                                              defaultValue = "ALL") BookingState state) {
        log.debug("Попытка пользователем {} получения информации о бронировании вещей пользователя.", userId);
        return bookingService.findAllOwnerBookings(userId, state);
    }
}