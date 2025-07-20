package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Optional<Booking> findBookingWithGraphById(Long bookingId);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByBooker_Id(Long bookerId);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Id(Long itemId);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_IdIn(List<Long> itemIds);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByBooker_IdAndStatusAndStartBeforeAndEndTimeAfter(Long bookerId,
                                                                                 BookingStatus bookingStatus,
                                                                                 LocalDateTime startAfter,
                                                                                 LocalDateTime endBefore);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByBooker_IdAndStatusAndEndTimeBefore(Long bookerId, BookingStatus bookingStatus,
                                                                    LocalDateTime endBefore);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByBooker_IdAndStatusAndStartAfter(@Param("id") Long bookerId,
                                                                 @Param("status") BookingStatus bookingStatus,
                                                                 @Param("today") LocalDateTime startAfter);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByBooker_IdAndStatus(Long bookerId, BookingStatus bookingStatus);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Owner_Id(Long ownerId);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Owner_IdAndStatusAndStartBeforeAndEndTimeAfter(Long ownerId,
                                                                                     BookingStatus bookingStatus,
                                                                                     LocalDateTime startBefore,
                                                                                     LocalDateTime endAfter);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Owner_IdAndStatusAndEndTimeBefore(Long ownerId, BookingStatus bookingStatus,
                                                                        LocalDateTime endBefore);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Owner_IdAndStatusAndStartAfter(Long ownerId, BookingStatus bookingStatus,
                                                                     LocalDateTime startAfter);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Collection<Booking> findAllByItem_Owner_IdAndStatus(Long ownerId, BookingStatus bookingStatus);

    @EntityGraph(attributePaths = {"item", "item.owner"})
    Optional<Booking> findByBooker_IdAndItem_IdAndStatusAndEndTimeBefore(Long bookerId, Long itemId, BookingStatus status,
                                                                         LocalDateTime endBefore);

    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.item.id = :id
            AND b.status = :status
            AND b.endTime >= :start
            AND b.start <= :end
            """)
    boolean hasOverlappingBooking(@Param("id") Long itemId,
                                  @Param("status") BookingStatus bookingStatus,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}