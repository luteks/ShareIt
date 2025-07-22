package ru.practicum.shareit.item_request.mapper;

import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item_request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.item_request.dto.ItemRequestDto;
import ru.practicum.shareit.item_request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated());
    }

    public static ItemRequestAllFieldsDto toItemRequestAllFieldsDto(ItemRequest itemRequest,
                                                                    Collection<Item> items) {
        return new ItemRequestAllFieldsDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                items.stream()
                        .map(ItemMapper::toItemRequestItemDto)
                        .toList());
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requestor(User.builder()
                        .id(userId)
                        .build())
                .created(LocalDateTime.now())
                .build();
    }
}