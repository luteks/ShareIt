package ru.practicum.shareit.item_request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item_request.client.ItemRequestClient;
import ru.practicum.shareit.item_request.dto.ItemRequestDto;
import ru.practicum.shareit.validation.Marker;

@Slf4j
@RestController
@Validated
@AllArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.debug("Попытка создания запроса на предмет {} пользователем {}.", itemRequestDto, userId);
        return itemRequestClient.createItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                  Integer from,
                                                  @Positive @RequestParam(name = "size", defaultValue = "10")
                                                  Integer size) {
        log.debug("Попытка получения запросов на предметы пользователем {}.", userId);
        return itemRequestClient.getItemRequests(userId, from, size);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
                                                     Integer from,
                                                     @Positive @RequestParam(name = "size", defaultValue = "10")
                                                     Integer size) {
        log.debug("Попытка получения запросов на предметы всех пользователей.");
        return itemRequestClient.getAllItemRequests(from, size, userId);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable(name = "requestId") Long requestId) {
        log.debug("Попытка получения запроса на предмет {} пользователем {}.", requestId, userId);
        return itemRequestClient.getItemRequest(requestId, userId);
    }
}