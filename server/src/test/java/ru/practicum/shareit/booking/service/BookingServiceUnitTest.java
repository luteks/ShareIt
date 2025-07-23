package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    private BookingService bookingService;

    private User owner;
    private User booker;
    private Item item;
    private BookingCreateDto bookingCreateDto;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
        owner = new User(1L, "Owner", "owner@mail.com");
        booker = new User(2L, "Booker", "booker@mail.com");
        item = new Item(1L, "Drill", "Electric", true, owner, null);

        LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
        LocalDateTime end = start.plusDays(1);
        bookingCreateDto = new BookingCreateDto(null, start, end, item.getId());

        booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);
    }

    @Test
    void testCreateBooking_Success() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.hasOverlappingBooking(eq(item.getId()), eq(BookingStatus.APPROVED),
                any(), any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.create(booker.getId(), bookingCreateDto);

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testGetBookingsByOwnerIdStatus_Success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(eq(owner.getId()),
                eq(BookingStatus.WAITING), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.WAITING,
                0, 10);

        assertEquals(1, list.size());
        assertEquals(booking.getId(), list.iterator().next().getId());
    }

    @Test
    void testGetBookingsByItem_Success() {
        when(bookingRepository.findBookingWithGraphById(booking.getId()))
                .thenReturn(Optional.of(booking));

        BookingDto found = bookingService.find(booker.getId(), booking.getId());

        assertEquals(booking.getId(), found.getId());
        assertEquals(item.getId(), found.getItem().getId());
    }

    @Test
    void testOwnerCannotBookOwnItem() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));


        assertThrows(ItemUnavailableException.class,
                () -> bookingService.create(owner.getId(), bookingCreateDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_UserNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.create(booker.getId(), bookingCreateDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ItemNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookingService.create(booker.getId(), bookingCreateDto));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testUpdateBooking_AccessDenied() {
        when(bookingRepository.findBookingWithGraphById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.update(booker.getId(),
                booking.getId(), true));
    }

    @Test
    void testGetBookingsByOwnerIdStateAll() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_Id(eq(owner.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.ALL,
                0, 10);

        assertEquals(1, list.size());
        assertEquals(booking.getId(), list.iterator().next().getId());
    }

    @Test
    void testGetBookingsByOwnerIdStateCurrent() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdAndStatusAndStartBeforeAndEndTimeAfter(eq(owner.getId()),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class), any(LocalDateTime.class),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.CURRENT,
                0, 10);

        assertTrue(list.isEmpty());
    }

    @Test
    void testGetBookingsByOwnerIdStatePast() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdAndStatusAndEndTimeBefore(eq(owner.getId()),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.PAST,
                0, 10);

        assertTrue(list.isEmpty());
    }

    @Test
    void testGetBookingsByOwnerIdStateFuture() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdAndStatusAndStartAfter(eq(owner.getId()),
                eq(BookingStatus.APPROVED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.FUTURE,
                0, 10);

        assertTrue(list.isEmpty());
    }

    @Test
    void testGetBookingsByOwnerIdStateRejected() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItem_Owner_IdAndStatus(eq(owner.getId()),
                eq(BookingStatus.REJECTED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<BookingDto> list = bookingService.findAllOwnerBookings(owner.getId(), BookingState.REJECTED,
                0, 10);

        assertTrue(list.isEmpty());
    }

    @Test
    void testCreateBooking_ItemUnavailable() {
        item.setAvailable(false);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(ItemUnavailableException.class, () ->
                bookingService.create(booker.getId(), bookingCreateDto));
    }

    @Test
    void testUpdateBookingNotFound() {
        when(bookingRepository.findBookingWithGraphById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                bookingService.update(owner.getId(), 99L, true));
    }

    @Test
    void testFindBookingNotFound() {
        when(bookingRepository.findBookingWithGraphById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                bookingService.find(booker.getId(), 99L));
    }

    @Test
    void testFindBookingAccessDenied() {
        when(bookingRepository.findBookingWithGraphById(anyLong()))
                .thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () ->
                bookingService.find(99L, booking.getId()));
    }

    @Test
    void testFindAllBookingsOwnerNotFound() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                bookingService.findAllOwnerBookings(99L, BookingState.ALL,
                        0, 10));
    }
}