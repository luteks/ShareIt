package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.Collection;

public interface BookingService {
    BookingDto create(BookingCreateDto bookingCreateDto, Long userId);

    BookingDto update(Long userId, Long bookingId, boolean approve);

    BookingDto find(Long userId, Long bookingId);

    Collection<BookingDto> findAllUserBookings(Long userId, BookingState state);

    Collection<BookingDto> findAllOwnerBookings(Long userId, BookingState state);
}
