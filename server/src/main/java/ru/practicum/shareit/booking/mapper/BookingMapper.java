package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


public class BookingMapper {
    public static BookingCreateDto toBookingCreateDto(Booking booking) {
        return new BookingCreateDto(booking.getId(),
                booking.getStart(),
                booking.getEndTime(),
                booking.getItem().getId());
    }

    public static BookingDto toBookingDto(Booking booking, Long userId) {
        return new BookingDto(booking.getId(),
                booking.getStart(),
                booking.getEndTime(),
                Item.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build(),
                User.builder()
                        .id(userId)
                        .build(),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingCreateDto bookingCreateDto, Long bookerId, String itemName) {
        return Booking.builder()
                .start(bookingCreateDto.getStart())
                .endTime(bookingCreateDto.getEnd())
                .item(Item.builder()
                        .id(bookingCreateDto.getItemId())
                        .name(itemName)
                        .build())
                .booker(User.builder()
                        .id(bookerId)
                        .build())
                .status(BookingStatus.WAITING)
                .build();
    }
}