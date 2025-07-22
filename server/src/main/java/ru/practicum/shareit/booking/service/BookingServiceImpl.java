package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.CommentCreationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemUnavailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.Pagination;
import ru.practicum.shareit.user.model.User;
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
    public BookingDto create(Long userId, BookingCreateDto bookingCreateDto) {
        checkUserExists(userId);
        Item item = fetchItemAndCheckAvailability(bookingCreateDto.getItemId());
        checkItemAvailability(bookingCreateDto, item, userId);

        Booking booking = BookingMapper.toBooking(bookingCreateDto, userId, item.getName());
        Booking savedBooking = bookingRepository.save(booking);

        BookingDto bookingDto = BookingMapper.toBookingDto(savedBooking, userId);

        log.debug("Создан новый запрос на бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, boolean approve) {
        Booking bookingUpdate = checkBookingExist(bookingId);

        checkBookingAccessForOwner(bookingUpdate, userId);
        checkBookingWaitingStatus(bookingUpdate.getStatus());

        bookingUpdate.setStatus(approve ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        BookingDto bookingDto = BookingMapper.toBookingDto(bookingRepository.save(bookingUpdate), bookingUpdate.getBooker().getId());

        log.debug("Изменен статус бронирования {}", bookingDto);
        return bookingDto;
    }

    @Override
    public BookingDto find(Long userId, Long bookingId) {
        Booking booking = checkBookingExist(bookingId);

        checkBookingAccessForUserOrOwner(booking, userId);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking, booking.getBooker().getId());

        log.debug("Найдено бронирование {}", bookingDto);
        return bookingDto;
    }

    @Override
    public Collection<BookingDto> findAllUserBookings(Long userId, BookingState state, Integer from, Integer size) {
        checkUserExists(userId);

        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<Booking> bookings = getBookingsByStateAndUser(userId, state, false, pageRequest);
        List<BookingDto> bookingDtoList = bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();
        ;

        log.debug("Найден список всех бронирований пользователя {}", bookingDtoList);
        return bookingDtoList;
    }

    @Override
    public Collection<BookingDto> findAllOwnerBookings(Long userId, BookingState state, Integer from, Integer size) {
        checkUserExists(userId);

        PageRequest pageRequest = Pagination.makePageRequest(from, size);
        Page<Booking> bookings = getBookingsByStateAndUser(userId, state, true, pageRequest);
        List<BookingDto> bookingDtoList = bookings.stream()
                .map(booking -> BookingMapper.toBookingDto(booking, userId))
                .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                .toList();


        log.debug("Найден список бронирования всех предметов пользователя {}", bookingDtoList);
        return bookingDtoList;
    }

    private Item fetchItemAndCheckAvailability(Long itemId) {
        Item item = checkItemExists(itemId);
        if (!item.getAvailable()) {
            log.error("Предмет с ID={} недоступен для бронирования", itemId);
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования", item.getId()));
        }
        return item;
    }

    private Booking checkBookingExist(Long bookingId) {
        return bookingRepository.findBookingWithGraphById(bookingId).orElseThrow(() -> {
            log.error("Бронирование {} не найдено.", bookingId);
            return new EntityNotFoundException("Бронирование", bookingId);
        });
    }

    private User checkUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь {} не найден!", userId);
                    return new EntityNotFoundException("Пользователь", userId);
                });
    }

    private Item checkItemExists(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> {
            log.error("Предмет {} не найден", id);
            return new EntityNotFoundException("Предмет", id);
        });
    }

    private void checkBookingAccessForOwner(Booking booking, Long userId) {
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.error("Подтвердить или отменить бронирование может только владелец вещи");
            throw new AccessDeniedException("Подтвердить или отменить бронирование может только владелец вещи");
        }
    }

    private void checkBookingAccessForUserOrOwner(Booking booking, Long userId) {
        if (!(booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId))) {
            log.error("Просмотреть бронирование может только владелец вещи либо автор бронирования");
            throw new AccessDeniedException("Просмотреть бронирование может только владелец вещи либо автор бронирования");
        }
    }

    private void checkItemAvailability(BookingCreateDto bookingCreateDto, Item item, Long userId) {
        LocalDateTime startBooking = bookingCreateDto.getStart();
        LocalDateTime endBooking = bookingCreateDto.getEnd();

        if (item.getOwner().getId().equals(userId) ||
                bookingRepository.hasOverlappingBooking(bookingCreateDto.getItemId(),
                        BookingStatus.APPROVED,
                        startBooking,
                        endBooking)) {
            log.error("Предмет с ID={} недоступен для бронирования", bookingCreateDto.getItemId());
            throw new ItemUnavailableException(String.format("Предмет с ID_%d недоступен для бронирования",
                    bookingCreateDto.getItemId()));
        }
    }

    private void checkBookingWaitingStatus(BookingStatus bookingStatus) {
        if (!bookingStatus.equals(BookingStatus.WAITING)) {
            log.error("Статус комментария не ожидание подтверждения.");
            throw new CommentCreationException("Статус комментария не ожидание подтверждения.");
        }
    }

    private Page<Booking> getBookingsByStateAndUser(Long userId, BookingState state, boolean isOwner, PageRequest pageRequest) {
        LocalDateTime now = LocalDateTime.now();

        if (isOwner) {
            return switch (state) {
                case ALL -> bookingRepository.findAllByItem_Owner_Id(userId, pageRequest);
                case CURRENT -> bookingRepository.findAllByItem_Owner_IdAndStatusAndStartBeforeAndEndTimeAfter(userId,
                        BookingStatus.APPROVED,
                        now, now, pageRequest);
                case PAST -> bookingRepository.findAllByItem_Owner_IdAndStatusAndEndTimeBefore(userId,
                        BookingStatus.APPROVED,
                        now, pageRequest);
                case FUTURE -> bookingRepository.findAllByItem_Owner_IdAndStatusAndStartAfter(userId,
                        BookingStatus.APPROVED,
                        now, pageRequest);
                case WAITING -> bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING,
                        pageRequest);
                case REJECTED -> bookingRepository.findAllByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED,
                        pageRequest);
                default -> new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
            };
        } else {
            return switch (state) {
                case ALL -> bookingRepository.findAllByBooker_Id(userId, pageRequest);
                case CURRENT -> bookingRepository.findAllByBooker_IdAndStatusAndStartBeforeAndEndTimeAfter(userId,
                        BookingStatus.APPROVED,
                        now, now, pageRequest);
                case PAST -> bookingRepository.findAllByBooker_IdAndStatusAndEndTimeBefore(userId,
                        BookingStatus.APPROVED,
                        now, pageRequest);
                case FUTURE -> bookingRepository.findAllByBooker_IdAndStatusAndStartAfter(userId,
                        BookingStatus.APPROVED,
                        now, pageRequest);
                case WAITING -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.WAITING,
                        pageRequest);
                case REJECTED -> bookingRepository.findAllByBooker_IdAndStatus(userId, BookingStatus.REJECTED,
                        pageRequest);
                default -> new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);
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