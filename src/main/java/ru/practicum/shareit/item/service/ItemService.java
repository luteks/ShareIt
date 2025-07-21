package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemAllFieldsDto find(Long itemId);

    Collection<ItemAllFieldsDto> findAll(Long userId);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    Collection<ItemDto> search(String text);

    CommentDto createComment(Long itemId, Long userId, CommentDto commentDto);
}
