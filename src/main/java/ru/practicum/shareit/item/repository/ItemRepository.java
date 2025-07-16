package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import java.util.Collection;

public interface ItemRepository {

    Item read(Long itemId);

    Collection<Item> readAll(Long userId);

    Item create(ItemDto itemDto, Long userId);

    Item update(ItemDto itemUpdateDto, Long itemId, Long userId);

    Collection<Item> search(String text);
}