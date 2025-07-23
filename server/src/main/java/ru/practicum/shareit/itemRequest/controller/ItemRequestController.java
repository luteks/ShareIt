package ru.practicum.shareit.itemRequest.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemRequest.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemRequest.service.ItemRequestService;

import java.util.Collection;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания запроса на предмет {} пользователем {}.", itemRequestDto, userId);
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public Collection<ItemRequestAllFieldsDto> getAllUserRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                                  @RequestParam(required = false) Integer from,
                                                                  @RequestParam(required = false) Integer size) {
        log.debug("Попытка получения запросов на предметы пользователем {}.", userId);
        return itemRequestService.findAllUserRequests(userId, from, size);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(required = false) Integer from,
                                             @RequestParam(required = false) Integer size) {
        log.debug("Попытка получения запросов на предметы всех пользователей.");
        return itemRequestService.findAll(userId, from, size);
    }

    @GetMapping("{requestId}")
    public ItemRequestAllFieldsDto get(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @PathVariable(name = "requestId") Long requestId) {
        log.debug("Попытка получения запроса на предмет {} пользователем {}.", requestId, userId);
        return itemRequestService.find(requestId, userId);
    }
}