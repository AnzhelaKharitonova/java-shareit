package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemToRequestDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RequestMapper requestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User requestor;
    private Request request1;
    private Request request2;
    private RequestCreateDto createDto;
    private RequestResponseDto responseDto1;
    private RequestResponseDto responseDto2;

    @BeforeEach
    void setUp() {
        requestor = User.builder().id(1L).name("User").email("user@test.com").build();

        createDto = RequestCreateDto.builder()
                .description("Нужна отвертка")
                .build();

        request1 = Request.builder()
                .id(1L)
                .description("Нужна отвертка")
                .requestor(requestor)
                .created(LocalDateTime.now().minusHours(2))
                .build();

        request2 = Request.builder()
                .id(2L)
                .description("Ищу дрель")
                .requestor(requestor)
                .created(LocalDateTime.now().minusHours(1)) // Более свежий по времени
                .build();

        responseDto1 = RequestResponseDto.builder()
                .id(1L)
                .description("Нужна отвертка")
                .created(request1.getCreated())
                .items(new ArrayList<>())
                .build();

        responseDto2 = RequestResponseDto.builder()
                .id(2L)
                .description("Ищу дрель")
                .created(request2.getCreated())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createRequest_Success() {
        // Given
        when(requestMapper.fromDto(createDto)).thenReturn(request1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.save(request1)).thenReturn(request1);
        when(requestMapper.toDto(request1)).thenReturn(responseDto1);

        RequestResponseDto result = requestService.createRequest(1L, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(requestRepository).save(request1);
    }

    @Test
    void createRequest_UserNotFound_ThrowsNotFoundException() {
        when(requestMapper.fromDto(createDto)).thenReturn(request1);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.createRequest(1L, createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найден пользователь с id 1");

        verify(requestRepository, never()).save(any());
    }

    @Test
    void findRequestsByUser_WhenExists_ShouldReturnSortedAndEnrichedList() {
        List<Request> mockRequests = List.of(request1, request2);
        Item item = Item.builder().id(10L).name("Отвертка").request(request1).build();
        ItemToRequestDto itemDto = ItemToRequestDto.builder().id(10L).name("Отвертка").build();

        when(requestRepository.findByRequestorId(1L)).thenReturn(mockRequests);
        when(requestMapper.toDtoCollection(mockRequests)).thenReturn(new ArrayList<>(List.of(responseDto1, responseDto2)));

        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of(item));
        when(itemMapper.toRequestDtoCollection(anyList())).thenReturn(List.of(itemDto));

        List<RequestResponseDto> result = requestService.findRequestsByUser(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(2L);
        assertThat(result.get(1).getId()).isEqualTo(1L);
        assertThat(result.get(1).getItems()).hasSize(1).contains(itemDto);
    }

    @Test
    void findRequestsByUser_WhenNoRequests_ReturnsEmptyList() {
        when(requestRepository.findByRequestorId(1L)).thenReturn(Collections.emptyList());

        List<RequestResponseDto> result = requestService.findRequestsByUser(1L);

        assertThat(result).isEmpty();
        verify(requestMapper, never()).toDtoCollection(any());
    }

    @Test
    void findAllRequests_ShouldCallRepositoryWithCorrectPagination() {
        when(requestRepository.findAllByRequestorIdNot(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(request2));
        when(requestMapper.toDtoCollection(anyList())).thenReturn(List.of(responseDto2));

        List<RequestResponseDto> result = requestService.findAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
        verify(requestRepository).findAllByRequestorIdNot(eq(1L), any(Pageable.class));
    }

    @Test
    void findRequestById_WhenExists_ShouldReturnRequestWithItems() {
        Item item = Item.builder().id(20L).name("Дрель").request(request1).build();
        ItemToRequestDto itemDto = ItemToRequestDto.builder().id(20L).name("Дрель").build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request1));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));
        when(requestMapper.toDto(request1)).thenReturn(responseDto1);
        when(itemMapper.toRequestDtoCollection(List.of(item))).thenReturn(List.of(itemDto));

        RequestResponseDto result = requestService.findRequestById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1).contains(itemDto);
    }

    @Test
    void findRequestById_WhenNotExists_ThrowsNotFoundException() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.findRequestById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Не найдена запрос с id 1");
    }
}
