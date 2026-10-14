package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;

    @Override
    public RequestResponseDto createRequest(Long userId, RequestCreateDto requestCreateDto) {
        Request request = requestMapper.fromDto(requestCreateDto);
        request.setRequestor(userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Ошибка при добавлении вещи, пользователь с id {} не найден", userId);
            return new NotFoundException("Не найден пользователь с id " + userId);
        }));
        request.setCreated(LocalDateTime.now());
        RequestResponseDto requestResponseDto = requestMapper.toDto(requestRepository.save(request));
        log.info("Добавлена новый запрос с id = {}", requestResponseDto.getId());
        return requestResponseDto;
    }

    @Override
    public List<RequestResponseDto> findRequestsByUser(Long userId) {
        List<Request> requestsByUser = requestRepository.findByRequestorId(userId);
        if (requestsByUser.isEmpty()) {
            return Collections.emptyList();
        }
        List<RequestResponseDto> dtoList = requestMapper.toDtoCollection(requestsByUser);
        enrichItemsToRequest(dtoList);

        return dtoList.stream()
                .sorted(Comparator.comparing(RequestResponseDto::getCreated).reversed())
                .toList();
    }

    @Override
    public List<RequestResponseDto> findAllRequests(Long userId, Integer from, Integer size) {
        int page = from / size;

        Pageable pageable = PageRequest.of(page, size, Sort.by("created").descending());

        List<Request> requests = requestRepository.findAllByRequestorIdNot(userId, pageable);

        return requestMapper.toDtoCollection(requests);
    }

    @Override
    public RequestResponseDto findRequestById(Long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> {
            log.warn("Запрос с id {} не найден", requestId);
            return new NotFoundException("Не найдена запрос с id " + requestId);
        });
        List<Item> itemsToRequest = itemRepository.findByRequestId(requestId);
        RequestResponseDto dto = requestMapper.toDto(request);
        dto.setItems(itemMapper.toRequestDtoCollection(itemsToRequest));
        return dto;
    }

    @Override
    public void enrichItemsToRequest(List<RequestResponseDto> dtoList) {
        List<Long> requestIds = dtoList.stream().map(RequestResponseDto::getId).toList();
        List<Item> items = itemRepository.findByRequestIdIn(requestIds);
        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        dtoList.forEach(dto -> {
            List<Item> itemsList = itemsByRequest.getOrDefault(dto.getId(), Collections.emptyList());
            List<ItemToRequestDto> itemToRequestDtoList = itemMapper.toRequestDtoCollection(itemsList);
            dto.setItems(itemToRequestDtoList);
        });
    }

}


