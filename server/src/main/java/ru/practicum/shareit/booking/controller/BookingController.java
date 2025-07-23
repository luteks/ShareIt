package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestBody BookingCreateDto bookingCreateDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка пользователем {} создания запроса бронирования {}.", userId, bookingCreateDto);
        return bookingService.create(userId, bookingCreateDto);
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
                                                             defaultValue = "ALL") BookingState state,
                                                     @RequestParam(required = false) Integer from,
                                                     @RequestParam(required = false) Integer size) {
        log.debug("Попытка пользователем {} получения информации о всех бронированиях.", userId);
        return bookingService.findAllUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(name = "state", required = false,
                                                              defaultValue = "ALL") BookingState state,
                                                      @RequestParam(required = false) Integer from,
                                                      @RequestParam(required = false) Integer size) {
        log.debug("Попытка пользователем {} получения информации о бронировании вещей пользователя.", userId);
        return bookingService.findAllOwnerBookings(userId, state, from, size);
    }
}