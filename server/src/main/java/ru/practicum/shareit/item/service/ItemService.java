package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemAllFieldsDto find(Long itemId, Long userId);

    Collection<ItemAllFieldsDto> findAll(Long userId, Integer from, Integer size);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto itemUpdateDto, Long itemId, Long userId);

    Collection<ItemDto> search(String text, Integer from, Integer size);

    CommentDto createComment(CommentDto commentDto, Long itemId, Long userId);
}
