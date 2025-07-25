package ru.practicum.shareit.itemRequest.service;

import ru.practicum.shareit.itemRequest.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.itemRequest.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestAllFieldsDto> findAllUserRequests(Long userId, Integer from, Integer size);

    Collection<ItemRequestDto> findAll(Long userId, Integer from, Integer size);

    ItemRequestAllFieldsDto find(Long requestId, Long userId);
}
