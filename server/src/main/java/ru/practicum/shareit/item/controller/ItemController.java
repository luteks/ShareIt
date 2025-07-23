package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestBody ItemDto itemDto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания предмета {}.", itemDto);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @PathVariable Long itemId) {
        log.debug("Попытка обновления предмета {}.", itemId);
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemAllFieldsDto get(@PathVariable Long itemId,
                                @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка поиска предмета {}.", itemId);
        return itemService.find(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> searchItems(@RequestParam(name = "text") String searchText,
                                           @RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(required = false) Integer from,
                                           @RequestParam(required = false) Integer size) {
        log.debug("Попытка создания предмета по описанию: {}.", searchText);
        return itemService.search(searchText, from, size);
    }

    @GetMapping
    public Collection<ItemAllFieldsDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        log.debug("Попытка поиска всех предметов.");
        return itemService.findAll(userId, from, size);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto create(@PathVariable Long itemId,
                             @RequestBody CommentDto commentDto,
                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания комментария к предмету {}.", itemId);
        return itemService.createComment(commentDto, itemId, userId);
    }
}