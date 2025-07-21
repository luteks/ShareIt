package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.CommentCreationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemAllFieldsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemAllFieldsDto find(Long itemId) {
        Item item = itemExistCheck(itemId);

        Collection<Booking> bookings = bookingRepository.findAllByItem_Id(itemId);
        Collection<CommentDto> comments = commentRepository.findAllByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        ItemAllFieldsDto itemAllFieldsDto = createItemAllFieldsDtoWithBookingsAndComments(item, bookings, comments);

        log.debug("Получен предмет {}.", itemAllFieldsDto);
        return itemAllFieldsDto;
    }

    @Override
    public Collection<ItemAllFieldsDto> findAll(Long userId) {
        userExistCheck(userId);

        Collection<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Booking>> bookingsMap = bookingRepository.findAllByItem_IdIn(itemIds).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        Map<Long, List<CommentDto>> commentsMap = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())));

        List<ItemAllFieldsDto> itemAllFieldsDtos = items.stream()
                .map(item -> {
                    List<Booking> bookings = bookingsMap.getOrDefault(item.getId(), Collections.emptyList());
                    List<CommentDto> comments = commentsMap.getOrDefault(item.getId(), Collections.emptyList());

                    return createItemAllFieldsDtoWithBookingsAndComments(item, bookings, comments);
                })
                .collect(Collectors.toList());

        log.debug("Получен список всех предметов {}.", itemAllFieldsDtos);

        return itemAllFieldsDtos;
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        userExistCheck(userId);
        ItemDto itemDtoNew = ItemMapper.toItemDto(itemRepository.save(ItemMapper.toItem(itemDto, userId)));

        log.debug("Создан новый предмет {}", itemDtoNew);
        return itemDtoNew;
    }

    @Override
    public ItemDto update(ItemDto itemUpdateDto, Long itemId, Long userId) {
        userExistCheck(userId);
        Item itemUpdate = itemExistCheck(itemId);

        itemOwnershipCheck(itemUpdate, userId);

        if (itemUpdateDto.getName() != null && !itemUpdateDto.getName().isBlank())
            itemUpdate.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null && !itemUpdateDto.getDescription().isBlank())
            itemUpdate.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) itemUpdate.setAvailable(itemUpdateDto.getAvailable());

        ItemDto itemDto = ItemMapper.toItemDto(itemRepository.save(itemUpdate));

        log.debug("Обновлен предмет {}.", itemDto);
        return itemDto;
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<Item> items = itemRepository.search(text);
        List<ItemDto> itemList = items.stream()
                .map(ItemMapper::toItemDto)
                .toList();

        log.debug("Получен через поиск список предметов по запросу '{}': {}.", text, itemList);

        return itemList;
    }

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        User author = userExistCheck(userId);

        Booking booking = bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(userId, itemId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now())
                .orElseThrow(() -> new CommentCreationException("Оставить комментарий может " +
                        "только пользователь, который брал вещь в аренду и только после окончания срока аренды"));

        CommentDto commentDtoNew = CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto, author, itemId)));

        log.debug("Создан комментарий {}.", commentDtoNew);
        return commentDtoNew;
    }

    private ItemAllFieldsDto createItemAllFieldsDtoWithBookingsAndComments(
            Item item,
            Collection<Booking> bookings,
            Collection<CommentDto> comments) {

        BookingDto endBooking = null;
        BookingDto startNextBooking = null;

        if (!bookings.isEmpty()) {
            endBooking = bookings.stream()
                    .filter(booking -> booking.getEndTime().isBefore(LocalDateTime.now()) &&
                            Duration.between(booking.getStart(), booking.getEndTime()).toSeconds() > 1)
                    .max(Comparator.comparing(Booking::getEndTime))
                    .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                    .orElse(null);

            startNextBooking = bookings.stream()
                    .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                    .orElse(null);
        }

        return ItemMapper.toItemAllFieldsDto(item, endBooking, startNextBooking, comments);
    }

    private User userExistCheck(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            log.error("Пользователь {} не найден!", id);
            return new EntityNotFoundException("Пользователь", id);
        });
    }

    private Item itemExistCheck(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> {
            log.error("Предмет {} не найден", id);
            return new EntityNotFoundException("Предмет", id);
        });
    }

    private void itemOwnershipCheck(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            log.error("Редактировать вещь {} может только владелец вещи!", item.getId());
            throw new AccessDeniedException("Редактировать вещь может только владелец вещи");
        }
    }
}