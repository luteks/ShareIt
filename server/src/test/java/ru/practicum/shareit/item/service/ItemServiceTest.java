package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.CommentCreationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTest {
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private UserDto userOwner;
    private ItemDto item;
    private UserDto booker;
    private BookingDto bookingDto;
    private LocalDateTime commentTime;

    @BeforeEach
    void setUp() {
        userOwner = userService.create(new UserDto(null, "Owner", "owner@gmail.com"));

        booker = userService.create(new UserDto(null, "Booker", "booker@mail.com"));

        item = itemService.create(new ItemDto(null, "Item", "about", true, null),
                userOwner.getId());

        LocalDateTime start = LocalDateTime.of(2025, 5, 11, 10, 0);
        LocalDateTime end = start.plusDays(1);
        commentTime = LocalDateTime.of(2025, 5, 14, 10, 0);

        bookingDto = bookingService.create(booker.getId(), new BookingCreateDto(null, start, end, item.getId()));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testCreateItem() {
        assertNotNull(item.getId());
        assertEquals("Item", item.getName());
    }

    @Test
    void testFindItemByOwner() {
        ItemAllFieldsDto fullItem = itemService.find(item.getId(), userOwner.getId());
        assertEquals(item.getId(), fullItem.getId());
        assertNotNull(fullItem.getLastBooking());
        assertNull(fullItem.getNextBooking());
        assertTrue(fullItem.getComments().isEmpty()); // Убедитесь, что комментарии пусты
    }

    @Test
    void testUpdateItemByOwner() {
        ItemDto updatedItem = itemService.update(new ItemDto(null, "Updated Item",
                "Updated Description", false, null), item.getId(), userOwner.getId());
        assertEquals("Updated Item", updatedItem.getName());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void testUpdateItemByNonOwner() {
        assertThrows(AccessDeniedException.class,
                () -> itemService.update(new ItemDto(null, "Invalid Update", "Invalid",
                        true, null), item.getId(), booker.getId())
        );
    }

    @Test
    void testFindItemsByOwner() {
        Collection<ItemAllFieldsDto> items = itemService.findAll(userOwner.getId(), 0, 10);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.iterator().next().getId());
    }

    @Test
    void testSearchItems() {
        Collection<ItemDto> foundItems = itemService.search("Item", 0, 10);
        assertEquals(1, foundItems.size());
        assertEquals("Item", foundItems.iterator().next().getName());
    }

    @Test
    void testCreateComment() {
        bookingService.update(userOwner.getId(), bookingDto.getId(), true);

        LocalDateTime commentTime = LocalDateTime.of(2025, 5, 14, 10, 0);

        CommentDto commentDto = itemService.createComment(new CommentDto(null,
                "comment", booker.getName(), commentTime), item.getId(), booker.getId());
        assertNotNull(commentDto.getId());
        assertEquals("comment", commentDto.getText());
    }

    @Test
    void testCreateCommentByNonBooker() {
        assertThrows(CommentCreationException.class,
                () -> itemService.createComment(new CommentDto(null,
                        "comment", booker.getName(), commentTime), item.getId(), booker.getId())
        );
    }

    @Test
    void testCreateCommentForNonExistentUser() {
        assertThrows(EntityNotFoundException.class,
                () -> itemService.createComment(new CommentDto(null,
                        "comment", booker.getName(), commentTime), item.getId(), 99L)
        );
    }

    @Test
    void testFindItemByNonExistentId() {
        assertThrows(EntityNotFoundException.class,
                () -> itemService.find(99L, userOwner.getId())
        );
    }
}