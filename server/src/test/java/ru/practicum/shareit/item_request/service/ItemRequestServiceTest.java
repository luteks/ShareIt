package ru.practicum.shareit.item_request.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item_request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.item_request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceTest {
    private final ItemRequestService itemRequestService;
    private final EntityManager entityManager;
    private final UserService userService;
    private ItemRequestDto itemRequestDto;
    private UserDto user;

    @BeforeEach
    void setUp() {
        var userDto = new UserDto(
                null,
                "John",
                "john@mail.com"
        );
        user = userService.create(userDto);
        itemRequestDto = ItemRequestDto.builder()
                .description("about")
                .build();
    }

    @Test
    void testCreateRequest_Success() {
        ItemRequestDto created = itemRequestService.create(itemRequestDto, user.getId());
        assertNotNull(created.getId());
        assertEquals("about", created.getDescription());
    }

    @Test
    void testFindById_Success() {
        ItemRequestDto created = itemRequestService.create(itemRequestDto, user.getId());

        ItemRequestAllFieldsDto found =
                itemRequestService.find(created.getId(), user.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("about", found.getDescription());
        assertTrue(found.getItems().isEmpty());
    }

    @Test
    void testCreate_UserNotFound() {
        assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.create(itemRequestDto, 999L)
        );
    }

    @Test
    void testFindAllUserRequests_WithItem() {
        ItemRequestDto created = itemRequestService.create(itemRequestDto, user.getId());

        var requestEntity = entityManager.find(
                ru.practicum.shareit.item_request.model.ItemRequest.class, created.getId());
        var userEntity = entityManager.find(
                ru.practicum.shareit.user.model.User.class, user.getId());

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .owner(userEntity)
                .request(requestEntity)
                .build();
        entityManager.persist(item);
        entityManager.flush();

        Collection<ItemRequestAllFieldsDto> requests =
                itemRequestService.findAllUserRequests(user.getId(), 0, 10);
        assertEquals(1, requests.size());
        ItemRequestAllFieldsDto dto = requests.iterator().next();
        assertEquals(1, dto.getItems().size());
        assertEquals("Drill", dto.getItems().iterator().next().getName());
    }

    @Test
    void testFindAll_OtherUsers() {
        UserDto other = userService.create(
                new UserDto(null, "Bob", "bob@mail.com"));
        itemRequestService.create(itemRequestDto, other.getId());

        Collection<ItemRequestDto> others =
                itemRequestService.findAll(user.getId(), 0, 10);
        assertEquals(1, others.size());
        assertEquals("about", others.iterator().next().getDescription());
    }
}