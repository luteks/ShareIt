package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemDto find(Long itemId) {
        Item item = itemExistCheck(itemId);

        log.debug("Получен предмет {}.", itemId);
        return ItemMapper.toItemDto(item);
    }

    public Collection<ItemDto> findAll(Long userId) {
        userExistCheck(userId);

        Collection<Item> items = itemRepository.readAll(userId);
        log.debug("Получен список всех предметов.");
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public ItemDto create(ItemDto itemDto, Long userId) {
        userExistCheck(userId);

        Item item = ItemMapper.toItem(itemDto, userId,0L);
        log.debug("Создан новый предмет {}", item);
        return ItemMapper.toItemDto(itemRepository.create(item, userId));
    }

    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        userExistCheck(userId);

        Item itemUpdate = itemExistCheck(itemId);

        itemOwnershipCheck(itemUpdate, userId);

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) itemUpdate.setName(itemDto.getName());
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) itemUpdate.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) itemUpdate.setAvailable(itemDto.getAvailable());

        log.debug("Обновлен предмет {}.", itemId);
        return ItemMapper.toItemDto(itemRepository.update(itemUpdate, itemId, userId));
    }

    public Collection<ItemDto> search(String text) {
        Collection<Item> items = itemRepository.search(text);
        log.debug("Получен через поиск список предметов по запросу: {}.", text);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private void userExistCheck(Long id) {
        if (userRepository.read(id).isEmpty()) {
            log.warn("Пользователь {} не найден!", id);
            throw new EntityNotFoundException("Пользователь", id);
        }
    }

    private Item itemExistCheck(Long id) {
        if (itemRepository.read(id).isEmpty()) {
            log.warn("Предмет {} не найден", id);
            throw new EntityNotFoundException("Предмет", id);
        }

        return itemRepository.read(id).get();
    }

    private void itemOwnershipCheck(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            log.warn("Редактировать вещь {} может только владелец вещи!", item.getId());
            throw new AccessDeniedException("Редактировать вещь может только владелец вещи");
        }
    }
}