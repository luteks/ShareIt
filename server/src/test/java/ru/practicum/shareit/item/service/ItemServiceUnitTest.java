package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CommentCreationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item_request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Booking booking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(userRepository, itemRepository, bookingRepository,
                commentRepository, itemRequestRepository);
        owner = new User(1L, "Owner", "owner@mail.com");
        booker = new User(2L, "Booker", "booker@mail.com");
        itemDto = new ItemDto(1L, "Drill", "Electric", true, null);
        item = new Item(1L, "Drill", "Electric", true, owner, null);

        LocalDateTime start = LocalDateTime.of(2025, 6, 11, 10, 0);
        LocalDateTime end = start.plusDays(1);

        booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);
        comment = new Comment(1L, "Nice", item, booker, LocalDateTime.now());
    }

    @Test
    void testCreateItem_Success() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemDto created = itemService.create(itemDto, owner.getId());
        assertNotNull(created);
        assertEquals(item.getName(), created.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void testFindItemAllFields_Success() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.findAllByItem_Id(anyLong())).thenReturn(List.of(booking));
        when(commentRepository.findAllByItemId(anyLong())).thenReturn(Collections.singletonList(comment));

        ItemAllFieldsDto dto = itemService.find(item.getId(), owner.getId());

        assertEquals(item.getId(), dto.getId());
        assertNull(dto.getNextBooking());
        assertFalse(dto.getComments().isEmpty());
    }


    @Test
    void testSearchItems_Found() {
        when(itemRepository.search(anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        Collection<ItemDto> found = itemService.search("drill", 0, 10);
        assertEquals(1, found.size());
    }

    @Test
    void testSearchItems_Empty() {
        when(itemRepository.search(anyString(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<ItemDto> found = itemService.search("none", 0, 10);
        assertTrue(found.isEmpty());
    }

    @Test
    void testCreateComment_Success() {
        booking.setStatus(BookingStatus.APPROVED);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(
                eq(booker.getId()), eq(item.getId()), eq(BookingStatus.APPROVED), any()))
                .thenReturn(Optional.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto dto = new CommentDto(null, "Nice", null, null);
        CommentDto created = itemService.createComment(dto, item.getId(), booker.getId());
        assertNotNull(created);
        assertEquals("Nice", created.getText());
    }

    @Test
    void testCreateComment_ByUserWithoutBooking_Throws() {
        booking.setStatus(BookingStatus.APPROVED);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(
                anyLong(), anyLong(), any(), any()))
                .thenReturn(Optional.empty());

        CommentDto dto = new CommentDto(null, "Try comment", null, null);
        assertThrows(CommentCreationException.class,
                () -> itemService.createComment(dto, item.getId(), booker.getId())
        );
    }

    @Test
    void testCreateComment_NonExistentUser_Throws() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommentDto dto = new CommentDto(null, "No user", null, null);
        assertThrows(EntityNotFoundException.class,
                () -> itemService.createComment(dto, item.getId(), 999L)
        );
    }

    @Test
    void testFindItemAllFields_NotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> itemService.find(999L, booker.getId())
        );
    }
}