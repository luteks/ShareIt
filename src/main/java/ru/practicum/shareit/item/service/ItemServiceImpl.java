package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

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

        ItemAllFieldsDto itemAllFieldsDto = createItemAllFieldsDtoWithBookings(item, bookings);

        log.debug("Получен предмет {}.", itemAllFieldsDto);
        return itemAllFieldsDto;
    }

    @Override
    public Collection<ItemAllFieldsDto> findAll(Long userId) {
        userExistCheck(userId);

        Collection<Item> items = itemRepository.findByOwnerId(userId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<Booking>> bookingsMap = bookingRepository.findAllByItem_IdIn(itemIds).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        List<ItemAllFieldsDto> itemAllFieldsDtos = items.stream()
                .map(item -> createItemAllFieldsDtoWithBookings(item, bookingsMap.getOrDefault(item.getId(), Collections.emptyList())))
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
    @Transactional
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userExistCheck(userId);
        Item item = itemExistCheck(itemId);
        boolean hasCompletedBooking = bookingRepository.findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now())
                .stream()
                .findAny()
                .isPresent();

        if (!hasCompletedBooking) {
            log.error("Пользователь не завершил бронирование данного товара.");
            throw new CommentCreationException("Пользователь не завершил бронирование данного товара.");
        }

        var comment = CommentMapper.toComment(commentDto, user, itemId);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private ItemAllFieldsDto createItemAllFieldsDtoWithBookings(Item item, Collection<Booking> bookings) {
        List<Booking> sortedBookings = bookings.stream()
                .sorted(comparing(Booking::getStart))
                .toList();

        BookingDto lastBooking = sortedBookings.stream()
                .filter(b -> b.getStart().isBefore(LocalDateTime.now()))
                .max(comparing(Booking::getStart))
                .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                .orElse(null);

        BookingDto nextBooking = sortedBookings.stream()
                .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                .min(comparing(Booking::getStart))
                .map(booking -> BookingMapper.toBookingDto(booking, booking.getBooker().getId()))
                .orElse(null);

        return ItemMapper.toItemAllFieldsDto(item, lastBooking, nextBooking,
                commentRepository.findAllByItemId(item.getId()).stream()
                        .map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList()));
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
