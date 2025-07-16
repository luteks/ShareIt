package ru.practicum.shareit.item.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

@Repository
@AllArgsConstructor
public class ItemRepositoryInMemory implements ItemRepository {
    private final HashMap<Long, Item> items = new HashMap<>();

    @Override
    public Item create(ItemDto itemDto, Long userId) {
        Item item = ItemMapper.toItem(itemDto, userId, getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(ItemDto itemUpdateDto, Long itemId, Long userId) {
        Item itemUpdate = read(itemId);
        if (itemUpdateDto.getName() != null) itemUpdate.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null) itemUpdate.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) itemUpdate.setAvailable(itemUpdateDto.getAvailable());

        items.put(itemUpdate.getId(), itemUpdate);
        return itemUpdate;
    }


    @Override
    public Item read(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public Collection<Item> readAll(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwner().getId(), userId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> {
                    return item.getName().toLowerCase().contains(searchText) ||
                            item.getDescription().toLowerCase().contains(searchText);
                })
                .toList();
    }

    private Long getNextId() {
        return items.isEmpty() ? 1 : Collections.max(items.keySet()) + 1;
    }
}