package ru.practicum.shareit.item.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.validation.Marker;

import java.util.Collection;

@Slf4j
@RestController
@Validated
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ItemDto create(@Valid @RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания предмета {}.", itemDto);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ItemDto update(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId) {
        log.debug("Попытка обновления предмета {}.", itemId);
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable Long itemId,
                       @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка поиска предмета {}.", itemId);
        return itemService.find(itemId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam(name = "text") @NonNull @NotBlank @NotEmpty String searchText,
                                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания предмета по описанию: {}.", searchText);
        return itemService.search(searchText);
    }

    @GetMapping
    public Collection<ItemDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка поиска всех предметов.");
        return itemService.findAll(userId);
    }
}
