package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User user;
    private Item item;
    private ItemCreateDto createDto;
    private ItemResponseDto responseDto;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@test.com").build();
        user = User.builder().id(2L).name("User").email("user@test.com").build();

        item = Item.builder()
                .id(1L)
                .name("Дрель")
                .description(" Bosch")
                .available(true)
                .owner(owner)
                .build();

        createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description(" Bosch")
                .available(true)
                .build();

        responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Дрель")
                .description(" Bosch")
                .available(true)
                .comments(new ArrayList<>())
                .build();
    }

    @Test
    void createItem_Success() {
        when(itemMapper.toItemFromCreateDto(createDto)).thenReturn(item);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.createItem(createDto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(itemRepository).save(item);
    }

    @Test
    void createItem_WithRequest_Success() {
        createDto.setRequestId(10L);
        Request mockRequest = Request.builder().id(10L).build();

        when(itemMapper.toItemFromCreateDto(createDto)).thenReturn(item);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(10L)).thenReturn(Optional.of(mockRequest));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.createItem(createDto, 1L);

        assertThat(result).isNotNull();
        verify(requestRepository).findById(10L);
    }

    @Test
    void createItem_UserNotFound_ThrowsNotFoundException() {
        when(itemMapper.toItemFromCreateDto(createDto)).thenReturn(item);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(createDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найден пользователь с id 1");
    }

    @Test
    void updateItem_Success() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder().name("Новое название").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertThat(result).isNotNull();
        verify(itemMapper).updateItemFromDto(updateDto, item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_NotOwner_ThrowsNotAvailableException() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder().name("Новое имя").build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Вызов от пользователя с ID 2, а владелец вещи — ID 1
        assertThatThrownBy(() -> itemService.updateItem(1L, 2L, updateDto))
                .isInstanceOf(NotAvailableException.class)
                .hasMessageContaining("Редактировать вещь может только ее владелец");
    }

    @Test
    void findItemById_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(Collections.emptyList());
        when(itemMapper.toItemResponseDto(item)).thenReturn(responseDto);
        when(commentMapper.toDtoCollection(any())).thenReturn(Collections.emptyList());

        ItemResponseDto result = itemService.findItemById(1L);

        assertThat(result).isNotNull();
        verify(commentRepository).findByItemId(1L);
    }

    @Test
    void search_ShouldReturnCollection() {
        when(itemRepository.search("дрель")).thenReturn(List.of(item));
        when(itemMapper.toItemResponseDtoCollection(any())).thenReturn(List.of(responseDto));

        Collection<ItemResponseDto> result = itemService.search("дрель");

        assertThat(result).hasSize(1);
        verify(itemRepository).search("дрель");
    }

    @Test
    void addComment_Success() {
        CommentDto commentDto = CommentDto.builder().text("Хорошая вещь").build();
        Booking completedBooking = Booking.builder()
                .id(1L)
                .end(LocalDateTime.now().minusDays(1)) // Бронирование завершено
                .status(Status.APPROVED)
                .build();
        Comment comment = Comment.builder().id(1L).text("Хорошая вещь").build();

        when(bookingRepository.findByItemIdAndBookerIdAndStatus(1L, 2L, Status.APPROVED))
                .thenReturn(List.of(completedBooking));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentMapper.fromDto(commentDto)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, commentDto);

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Хорошая вещь");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_NoCompletedBooking_ThrowsBadRequestException() {
        CommentDto commentDto = CommentDto.builder().text("Аренда еще не завершена").build();
        Booking activeBooking = Booking.builder()
                .id(1L)
                .end(LocalDateTime.now().plusDays(1)) // Бронирование еще активно / в будущем
                .status(Status.APPROVED)
                .build();

        when(bookingRepository.findByItemIdAndBookerIdAndStatus(1L, 2L, Status.APPROVED))
                .thenReturn(List.of(activeBooking));

        assertThatThrownBy(() -> itemService.addComment(2L, 1L, commentDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Отзыв можно оставить только после завершения аренды");
    }

    @Test
    void createItem_RequestNotFound_ThrowsNotFoundException() {
        createDto.setRequestId(999L);
        when(itemMapper.toItemFromCreateDto(createDto)).thenReturn(item);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(createDto, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найден запрос с id 999");
    }

    @Test
    void updateItem_ItemNotFound_ThrowsNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(999L, 1L, ItemUpdateDto.builder().build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Вещь с id 999 не найдена");
    }

    @Test
    void findItemById_ItemNotFound_ThrowsNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findItemById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найдена вещь с id 999");
    }

    @Test
    void findItemsByOwner_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findItemsByOwner(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найден пользователь с id 999");
    }

    @Test
    void findItemsByOwner_WhenNoItems_ReturnsEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(1L)).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.findItemsByOwner(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void findItemsByOwner_Success_WithEnrichment() {
        List<Item> items = List.of(item);
        List<ItemResponseDto> dtoList = new ArrayList<>(List.of(responseDto));

        Comment comment = Comment.builder().id(1L).item(item).text("Тест").build();
        List<Comment> comments = List.of(comment);

        Booking lastBooking = Booking.builder()
                .id(10L)
                .item(item)
                .start(LocalDateTime.now().minusDays(2))
                .status(Status.APPROVED)
                .build();

        Booking nextBooking = Booking.builder()
                .id(11L)
                .item(item)
                .start(LocalDateTime.now().plusDays(2))
                .status(Status.APPROVED)
                .build();

        List<Booking> bookings = List.of(lastBooking, nextBooking);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerId(1L)).thenReturn(items);
        when(itemMapper.toItemResponseDtoCollection(items)).thenReturn(dtoList);

        when(commentRepository.findByItemIdIn(anyList())).thenReturn(comments);
        when(commentMapper.toDtoCollection(anyList())).thenReturn(List.of(CommentDto.builder().text("Тест").build()));
        when(bookingRepository.findByItemIdInAndStatus(anyList(), eq(Status.APPROVED))).thenReturn(bookings);

        List<ItemResponseDto> result = itemService.findItemsByOwner(1L);

        assertThat(result).hasSize(1);
        verify(commentRepository).findByItemIdIn(anyList());
        verify(bookingRepository).findByItemIdInAndStatus(anyList(), eq(Status.APPROVED));
        verify(bookingMapper, times(2)).toBookingShortDto(any());
    }
}
