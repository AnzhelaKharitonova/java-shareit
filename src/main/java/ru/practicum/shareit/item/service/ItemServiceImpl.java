package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private ItemMapper itemMapper;
    private BookingMapper bookingMapper;
    private CommentMapper commentMapper;

    @Override
    public ItemResponseDto createItem(ItemCreateDto itemCreateDto, Long userId) {
        Item item = itemMapper.toItemFromCreateDto(itemCreateDto);
        item.setOwner(userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Ошибка при добавлении вещи, пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        }));
        ItemResponseDto itemResponseDto = itemMapper.toItemResponseDto(itemRepository.save(item));
        log.info("Добавлена новая вещь с id = {}", itemResponseDto.getId());
        return itemResponseDto;
    }

    @Override
    public ItemResponseDto updateItem(Long itemId, Long userId, ItemUpdateDto itemUpdateDto) {

        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Ошибка при обновлении: вещь с id {} не найдена", itemId);
            return new NotFoundException("Вещь с id " + itemId + " не найдена");
        });

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Ошибка при редактировании вещи id={}, пользователь id={} не является владельцем", itemId, userId);
            throw new NotAvailableException("Редактировать вещь может только ее владелец!");
        }
        itemMapper.updateItemFromDto(itemUpdateDto, item);
        Item updatedItem = itemRepository.save(item);
        log.info("Обновлена информация о вещи с id {}", itemId);

        return itemMapper.toItemResponseDto(updatedItem);
    }

    @Override
    public ItemResponseDto findItemById(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Вещь с id {} не найдена", itemId);
            return new NotFoundException("Не найдена вещь с id " + itemId);
        });
        List<Comment> comments = commentRepository.findByItemId(itemId);
        ItemResponseDto dto = itemMapper.toItemResponseDto(item);
        dto.setComments(commentMapper.toDtoCollection(comments));
        return dto;
    }

    @Override
    public List<ItemResponseDto> findItemsByOwner(Long userId) {

        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Ошибка при просмотре списка вещей, пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        });

        List<Item> items = itemRepository.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<ItemResponseDto> dtoList = itemMapper.toItemResponseDtoCollection(items);
        enrichComments(dtoList);
        enrichBookings(dtoList);

        return dtoList;
    }

    @Override
    public Collection<ItemResponseDto> search(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemMapper.toItemResponseDtoCollection(itemRepository.search(text));
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED);
        boolean hasCompletedBooking = bookings.stream()
                .anyMatch(booking -> booking.getEnd().isBefore(now));
        if (!hasCompletedBooking) {
            log.warn("Пользователь {} не имеет завершенных бронирований для вещи {}", userId, itemId);
            throw new BadRequestException("Отзыв можно оставить только после завершения аренды вещи");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        });
        Item item = itemRepository.findById(itemId).orElseThrow(() -> {
            log.warn("Вещь с id {} не найдена", itemId);
            return new NotFoundException("Не найдена вещь с id " + itemId);
        });

        Comment comment = commentMapper.fromDto(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toDto(savedComment);
    }

    private void enrichComments(List<ItemResponseDto> dtoList) {
        List<Long> itemIds = dtoList.stream().map(ItemResponseDto::getId).toList();
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsByItem = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));


        dtoList.forEach(dto -> {
            List<Comment> commentsList = commentsByItem.getOrDefault(dto.getId(), Collections.emptyList());
            List<CommentDto> commentDtoList = commentMapper.toDtoCollection(commentsList);
            dto.setComments(commentDtoList);
        });
    }

    private void enrichBookings(List<ItemResponseDto> dtoList) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = dtoList.stream()
                .map(ItemResponseDto::getId)
                .toList();

        Collection<Booking> bookings = bookingRepository.findByItemIdInAndStatus(itemIds, Status.APPROVED);

        Map<Long, List<Booking>> bookingsByItem = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));


        dtoList.forEach(itemDto -> {

            List<Booking> itemBookings = bookingsByItem.getOrDefault(itemDto.getId(), Collections.emptyList());

            Booking last = itemBookings.stream()
                    .filter(b -> !b.getStart().isAfter(now))
                    .max(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            Booking next = itemBookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .orElse(null);

            itemDto.setLastBooking(bookingMapper.toBookingShortDto(last));
            itemDto.setNextBooking(bookingMapper.toBookingShortDto(next));
        });
    }

}
