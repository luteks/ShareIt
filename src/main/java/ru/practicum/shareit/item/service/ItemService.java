package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto find(Long itemId);

    Collection<ItemDto> findAll(Long userId);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    Collection<ItemDto> search(String text);
}
