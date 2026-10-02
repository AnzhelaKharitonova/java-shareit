package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User booker;
    private User owner;
    private Item item;

    private Booking pastBooking;
    private Booking currentBooking;
    private Booking futureBooking;

    @BeforeEach
    void setUp() {
        owner = User.builder().name("Owner").email("owner@test.com").build();
        booker = User.builder().name("Booker").email("booker@test.com").build();
        owner = userRepository.save(owner);
        booker = userRepository.save(booker);

        item = Item.builder().name("Дрель").description("Bosch").available(true).owner(owner).build();
        item = itemRepository.save(item);

        LocalDateTime now = LocalDateTime.now();

        pastBooking = Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(now.minusDays(5)).end(now.minusDays(3))
                .build();

        currentBooking = Booking.builder()
                .item(item).booker(booker).status(Status.APPROVED)
                .start(now.minusDays(1)).end(now.plusDays(1))
                .build();

        futureBooking = Booking.builder()
                .item(item).booker(booker).status(Status.WAITING)
                .start(now.plusDays(3)).end(now.plusDays(5))
                .build();

        bookingRepository.save(pastBooking);
        bookingRepository.save(currentBooking);
        bookingRepository.save(futureBooking);
    }

    @Test
    void findByBookerIdAndStartBeforeAndEndAfter_ShouldReturnCurrentBooking() {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").ascending());

        List<Booking> result = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                booker.getId(), now, now, pageable
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void findByBookerIdAndEndBefore_ShouldReturnPastBooking() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").ascending());

        List<Booking> result = bookingRepository.findByBookerIdAndEndBefore(booker.getId(), now, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    void findByBookerIdAndStartAfter_ShouldReturnFutureBooking() {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").ascending());

        List<Booking> result = bookingRepository.findByBookerIdAndStartAfter(booker.getId(), now, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(futureBooking.getId());
    }

    @Test
    void findByItemIdInAndStatus_ShouldReturnMatchingBookings() {
        List<Long> itemIds = List.of(item.getId());
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").ascending());

        List<Booking> result = bookingRepository.findByItemIdInAndStatus(itemIds, Status.WAITING, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Status.WAITING);
        assertThat(result.get(0).getId()).isEqualTo(futureBooking.getId());
    }

    @Test
    void findByItemIdAndBookerIdAndStatus_ShouldReturnApprovedBookingsForCommentValidation() {
        List<Booking> result = bookingRepository.findByItemIdAndBookerIdAndStatus(
                item.getId(), booker.getId(), Status.APPROVED
        );

        assertThat(result).hasSize(2)
                .extracting(Booking::getStatus)
                .containsOnly(Status.APPROVED);
    }
}
