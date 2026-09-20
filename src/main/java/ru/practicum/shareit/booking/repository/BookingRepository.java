package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long userId);

    List<Booking> findByItemIdIn(List<Long> itemIds);

    List<Booking> findByItemIdAndBookerIdAndStatus(Long itemId, Long userId, Status status);

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, Status status);

}
