package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.itemRequest.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static ItemRequestItemDto toItemRequestItemDto(Item item) {
        return new ItemRequestItemDto(item.getId(),
                item.getName(),
                item.getOwner().getId());
    }

    public static ItemAllFieldsDto toItemAllFieldsDto(Item item,
                                                      BookingDto endBooking,
                                                      BookingDto startNextBooking,
                                                      Collection<CommentDto> comments) {
        return new ItemAllFieldsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                endBooking,
                startNextBooking,
                comments
        );
    }

    public static Item toItem(ItemDto itemDto, Long userId) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(new User(userId))
                .request(itemDto.getRequestId() != null ?
                        ItemRequest.builder().id(itemDto.getRequestId()).build() : null)
                .build();
    }
}