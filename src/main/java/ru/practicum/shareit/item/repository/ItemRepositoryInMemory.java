package ru.practicum.shareit.item.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
@AllArgsConstructor
public class ItemRepositoryInMemory implements ItemRepository {
    private final HashMap<Long, Item> items = new HashMap<>();

    @Override
    public Item create(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item itemUpdate, Long itemId) {
        items.put(itemUpdate.getId(), itemUpdate);
        return itemUpdate;
    }


    @Override
    public Optional<Item> read(Long itemId) {
        if (!items.containsKey(itemId)) {
            return Optional.empty();
        }
        return Optional.of(items.get(itemId));
    }

    @Override
    public Collection<Item> readAll(Long userId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), userId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() != null && item.getAvailable())
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .toList();
    }

    private Long getNextId() {
        return items.isEmpty() ? 1 : Collections.max(items.keySet()) + 1;
    }
}