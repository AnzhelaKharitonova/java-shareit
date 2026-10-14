package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByItemIdAndBookerIdAndStatus(Long itemId, Long userId, Status status);

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, Status status);

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Pageable pageable);

    List<Booking> findByItemIdIn(List<Long> itemIds, Pageable pageable);

    List<Booking> findByItemIdInAndStartBeforeAndEndAfter(List<Long> itemIds, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemIdInAndEndBefore(List<Long> itemIds, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemIdInAndStartAfter(List<Long> itemIds, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, Status status, Pageable pageable);

}
