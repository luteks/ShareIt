package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {

    Optional<Item> read(Long itemId);

    Collection<Item> readAll(Long userId);

    Item create(Item item);

    Item update(Item itemUpdate, Long itemId);

    Collection<Item> search(String text);
}