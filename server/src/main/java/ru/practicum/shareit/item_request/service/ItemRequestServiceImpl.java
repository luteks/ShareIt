package ru.practicum.shareit.item_request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item_request.dto.ItemRequestAllFieldsDto;
import ru.practicum.shareit.item_request.dto.ItemRequestDto;
import ru.practicum.shareit.item_request.mapper.ItemRequestMapper;
import ru.practicum.shareit.item_request.model.ItemRequest;
import ru.practicum.shareit.item_request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.Pagination;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        userExistCheck(userId);

        itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequestRepository
                .save(ItemRequestMapper.toItemRequest(itemRequestDto, userId)));
        log.debug("Создание запроса на предмет {} пользователем {}.", itemRequestDto, userId);

        return itemRequestDto;
    }

    @Override
    public Collection<ItemRequestAllFieldsDto> findAllUserRequests(Long userId, Integer from, Integer size) {
        userExistCheck(userId);

        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<ItemRequest> userRequests;

        userRequests = itemRequestRepository.findAllByRequestor_IdOrderByCreatedDesc(userId,
                Objects.requireNonNullElseGet(pageRequest, () -> PageRequest.of(0, Integer.MAX_VALUE)));
        Collection<Long> requestsIds = userRequests.stream()
                .map(ItemRequest::getId)
                .toList();

        Collection<Item> itemRequests = itemRepository.findAllByRequest_IdIn(requestsIds);

        Map<Long, List<Item>> itemsByRequest = itemRequests.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        List<ItemRequestAllFieldsDto> itemRequestAllFieldsDtos = userRequests.stream()
                .map(itemRequest -> {
                    List<Item> relatedItems = itemsByRequest.getOrDefault(itemRequest.getId(), Collections.emptyList());
                    return ItemRequestMapper.toItemRequestAllFieldsDto(itemRequest, relatedItems);
                })
                .toList();

        log.debug("Получение списка запросов на предметы пользователем {}: {}.", userId, itemRequestAllFieldsDtos);
        return itemRequestAllFieldsDtos;
    }

    @Override
    public Collection<ItemRequestDto> findAll(Long userId, Integer from, Integer size) {
        userExistCheck(userId);

        PageRequest pageRequest = Pagination.makePageRequest(from, size);

        List<ItemRequestDto> itemRequestDtos = itemRequestRepository.findAllByRequestor_IdNotOrderByCreatedDesc(userId,
                        Objects.requireNonNullElseGet(pageRequest, () ->
                                PageRequest.of(0, Integer.MAX_VALUE))).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();

        log.debug("Получение списка запросов на предметы всех пользователей: {}.", itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public ItemRequestAllFieldsDto find(Long requestId, Long userId) {
        userExistCheck(userId);
        ItemRequest itemRequest = itemRequestExistCheck(requestId);

        Collection<Item> itemsRequest = itemRepository.findAllByRequest_Id(requestId);

        ItemRequestAllFieldsDto itemRequestAllFieldsDto = ItemRequestMapper.toItemRequestAllFieldsDto(itemRequest, itemsRequest);
        log.debug("Получения запроса на предмет {} пользователем {}.", itemRequestAllFieldsDto, userId);

        return itemRequestAllFieldsDto;
    }

    private User userExistCheck(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь {} не найден!", id);
            return new EntityNotFoundException("Пользователь", id);
        });
    }

    private ItemRequest itemRequestExistCheck(Long id) {
        return itemRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Запрос вещи {} не найден.", id);
                    return new EntityNotFoundException("Запрос вещи", id);
                });
    }
}