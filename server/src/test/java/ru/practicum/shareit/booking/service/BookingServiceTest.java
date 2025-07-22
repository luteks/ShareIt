package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceTest {
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    private BookingDto bookingDto;
    private BookingCreateDto bookingCreateDto;
    private ItemDto item;
    private UserDto userOwner;
    private UserDto booker;
    private final EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userOwner = userService.create(new UserDto(null, "Owner", "owner@gmail.com"));

        booker = userService.create(new UserDto(null, "Booker", "booker@mail.com"));

        item = itemService.create(new ItemDto(null, "Item", "about", true, null),
                userOwner.getId());

        LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
        LocalDateTime end = start.plusDays(1);

        bookingCreateDto = new BookingCreateDto(null, start, end, item.getId());
        bookingDto = bookingService.create(booker.getId(), bookingCreateDto);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void saveTest() {
        assertNotNull(bookingDto.getId());
        assertEquals(bookingCreateDto.getStart(), bookingDto.getStart());
        assertEquals(bookingCreateDto.getEnd(), bookingDto.getEnd());
    }

    @Test
    void getAllBookingsTest() {

        Collection<BookingDto> bookings = bookingService.findAllUserBookings(booker.getId(), BookingState.WAITING,
                0, 10);

        assertEquals(1, bookings.size());
        assertEquals(bookingDto.getId(), bookings.iterator().next().getId());
    }

    @Test
    void getBookingsByOwnerIdTest() {
        Collection<BookingDto> bookings = bookingService.findAllOwnerBookings(userOwner.getId(), BookingState.WAITING,
                0, 10);

        assertEquals(1, bookings.size());
        assertEquals(bookingDto.getId(), bookings.iterator().next().getId());
    }

    @Test
    void getBookingByIdTest() {
        BookingDto booking = bookingService.find(userOwner.getId(), bookingDto.getId());

        assertNotNull(booking);
        assertEquals(bookingDto.getId(), booking.getId());
        assertEquals(item.getId(), booking.getItem().getId());
    }

    @Test
    void approveBookingTest() {
        BookingDto approved = bookingService.update(userOwner.getId(), bookingDto.getId(), true);

        BookingDto approvedBooking = bookingService.find(userOwner.getId(), bookingDto.getId());
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void getAllBookingsEmptyListTest() {
        Collection<BookingDto> all = bookingService.findAllUserBookings(booker.getId(), BookingState.REJECTED,
                0, 10);
        assertTrue(all.isEmpty());
    }

    @Test
    void findMethodAccessAsOwnerOrBookerTest() {
        BookingDto asBooker = bookingService.find(booker.getId(), bookingDto.getId());

        BookingDto asOwner = bookingService.find(userOwner.getId(), bookingDto.getId());

        assertNotNull(asBooker);
        assertNotNull(asOwner);
    }

    @Test
    void ownerCannotBookOwnItemTest() {
        assertThrows(ItemUnavailableException.class,
                () -> bookingService.create(userOwner.getId(), bookingCreateDto),
                "Предмет с ID_1 недоступен для бронирования"
        );
    }
}