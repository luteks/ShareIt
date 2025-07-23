package ru.practicum.shareit.itemRequest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.itemRequest.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemRequest.mapper.ItemRequestMapper;
import ru.practicum.shareit.itemRequest.model.ItemRequest;
import ru.practicum.shareit.itemRequest.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceUnitTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    private ItemRequestService itemRequestService;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;
    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("marry")
                .email("marry@mail.com")
                .build();
        user = new User(userDto.getId(), userDto.getName(), userDto.getEmail());
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("my request")
                .created(LocalDateTime.now())
                .build();
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userRepository, itemRepository);
        itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, userDto.getId());
        itemRequest.setId(itemRequestDto.getId());
    }

    @Test
    void testCreate_Success() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto created = itemRequestService.create(itemRequestDto, userDto.getId());

        assertEquals(itemRequest.getId(), created.getId());
        assertEquals(itemRequest.getDescription(), created.getDescription());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void testCreate_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.create(itemRequestDto, 99L));
    }

    @Test
    void testFindById_Success() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRepository.findAllByRequest_Id(itemRequest.getId()))
                .thenReturn(List.of(new Item(1L, "Drill", "Electric",
                        true, user, itemRequest)));

        ItemRequestAllFieldsDto found =
                itemRequestService.find(itemRequest.getId(), userDto.getId());

        assertEquals(itemRequest.getId(), found.getId());
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().iterator().next().getName());
        verify(itemRequestRepository, times(1)).findById(itemRequest.getId());
        verify(itemRepository, times(1)).findAllByRequest_Id(itemRequest.getId());
    }

    @Test
    void testFindById_UserNotFound() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.find(itemRequest.getId(), userDto.getId()));
    }

    @Test
    void testFindById_NotFound() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.find(itemRequest.getId(), userDto.getId()));
    }

    @Test
    void testFindAllUserRequests_Success() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(
                eq(userDto.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(singletonList(itemRequest)));
        Item i = Item.builder()
                .id(2L)
                .name("Saw")
                .description("Hand saw")
                .available(true)
                .owner(user)
                .request(ItemRequest.builder()
                        .id(itemRequest.getId())
                        .created(itemRequest.getCreated())
                        .requestor(user)
                        .description(itemRequest.getDescription())
                        .build())
                .build();

        when(itemRepository.findAllByRequest_IdIn(anyCollection()))
                .thenReturn(List.of(i));

        Collection<ItemRequestAllFieldsDto> list =
                itemRequestService.findAllUserRequests(userDto.getId(), 0, 10);

        assertEquals(1, list.size());
        ItemRequestAllFieldsDto dto = list.iterator().next();
        assertEquals(1, dto.getItems().size());
        assertEquals("Saw", dto.getItems().iterator().next().getName());

        verify(itemRequestRepository)
                .findAllByRequestor_IdOrderByCreatedDesc(eq(userDto.getId()), any(PageRequest.class));
        verify(itemRepository)
                .findAllByRequest_IdIn(anyCollection());
    }

    @Test
    void testFindAllUserRequests_UserNotFound() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.findAllUserRequests(userDto.getId(), 0, 10));
    }

    @Test
    void testFindAllOtherUsers_Success() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(
                eq(userDto.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(singletonList(itemRequest)));

        Collection<ItemRequestDto> others =
                itemRequestService.findAll(userDto.getId(), 0, 10);

        assertEquals(1, others.size());
    }

    @Test
    void testFindAllOtherUsers_Empty() {
        when(userRepository.findById(userDto.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(
                eq(userDto.getId()), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Collection<ItemRequestDto> others =
                itemRequestService.findAll(userDto.getId(), 0, 10);

        assertTrue(others.isEmpty());
    }
}