package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto create(BookingCreateDto bookingCreateDto, Long userId) {
        checkUserExists(userId);
        Item item = fetchItemAndCheckAvailability(bookingCreateDto.getItemId());
        checkItemAvailability(bookingCreateDto);

        Booking booking = BookingMapper.toBooking(bookingCreateDto, userId, item.getName());
        Booking savedBooking = bookingRepository.save(booking);

        BookingDto bookingDto = BookingMapper.toBookingDto(savedBooking, userId);

        log.debug("Создан новый запрос на бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, boolean approve) {
        Booking bookingUpdate = bookingRepository.findBookingWithGraphById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        checkBookingAccessForOwner(bookingUpdate, userId);
        bookingUpdate.setStatus(approve ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(bookingUpdate), bookingUpdate.getBooker().getId());

        log.debug("Изменен статус бронирования {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto find(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findBookingWithGraphById(bookingId).orElseThrow(() ->
                new EntityNotFoundException("Бронирование", bookingId));

        checkBookingAccessForUserOrOwner(booking, userId);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking, booking.getBooker().getId());

        log.debug("Найдено бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public Collection<BookingDto> findAllUserBookings(Long userId, BookingState state) {
        Collection<Booking> bookings = getBookingsByStateAndUser(userId, state, false);
        List<BookingDto> bookingDtoList = mapBookingsToDtoList(bookings, userId);
        log.debug("Найден список всех бронирований пользователя {}", bookingDtoList);
        return bookingDtoList;
    }

    @Override
    public Collection<BookingDto> findAllOwnerBookings(Long userId, BookingState state) {
        checkUserExists(userId);
        Collection<Booking> bookings = getBookingsByStateAndUser(userId, state, true);
        List<BookingDto> bookingDtoList = mapBookingsToDtoList(bookings, userId);


        log.debug("Найден список бронирования всех предметов пользователя {}", bookingDtoList);
        return bookingDtoList;
    }

    private Item fetchItemAndCheckAvailability(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new EntityNotFoundException("Предмет", itemId));
        if (!item.getAvailable()) {
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования", item.getId()));
        }
        return item;
    }

    private void checkUserExists(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException("Пользователь", userId));
    }

    private void checkBookingAccessForOwner(Booking booking, Long userId) {
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить или отменить бронирование может только владелец вещи");
        }
    }

    private void checkBookingAccessForUserOrOwner(Booking booking, Long userId) {
        if (!(booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId))) {
            throw new AccessDeniedException("Просмотреть бронирование может только владелец вещи либо автор бронирования");
        }
    }

    private void checkItemAvailability(BookingCreateDto bookingCreateDto) {
        LocalDateTime startBooking = bookingCreateDto.getStart();
        LocalDateTime endBooking = bookingCreateDto.getEnd();

        if (bookingRepository.hasOverlappingBooking(bookingCreateDto.getItemId(),
                BookingStatus.APPROVED,
                startBooking,
                endBooking)) {
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования",
                    bookingCreateDto.getItemId()));
        }
    }

    private Collection<Booking> getBookingsByStateAndUser(Long userId, BookingState state, boolean isOwner) {
        LocalDateTime now = LocalDateTime.now();
        if (isOwner) {
            return switch (state) {
                case ALL -> bookingRepository.findAllByItem_Owner_Id(userId);
                case CURRENT ->
                        bookingRepository.findAllByItem_Owner_IdAndStatusAndStartBeforeAndEndTimeAfter(userId, BookingStatus.APPROVED, now, now);
                case PAST ->
                        bookingRepository.findAllByItem_Owner_IdAndStatusAndEndTimeBefore(userId, BookingStatus.APPROVED, now);
                case FUTURE ->
                        bookingRepository.findAllByItem_Owner_IdAndStatusAndStartAfter(userId, BookingStatus.APPROVED, now);
                case WAITING -> bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING);
                case REJECTED -> bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED);
                default -> Collections.emptyList();
            };
        } else {
            return switch (state) {
                case ALL -> bookingRepository.findAllByBooker_Id(userId);
                case CURRENT ->
                        bookingRepository.findAllByBooker_IdAndStatusAndStartBeforeAndEndTimeAfter(userId, BookingStatus.APPROVED, now, now);
                case PAST ->
                        bookingRepository.findAllByBooker_IdAndStatusAndEndTimeBefore(userId, BookingStatus.APPROVED, now);
                case FUTURE ->
                        bookingRepository.findAllByBooker_IdAndStatusAndStartAfter(userId, BookingStatus.APPROVED, now);
                case WAITING -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.WAITING);
                case REJECTED -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.REJECTED);
                default -> Collections.emptyList();
            };
        }
    }

    private List<BookingDto> mapBookingsToDtoList(Collection<Booking> bookings, Long userId) {
        return bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
    }
}