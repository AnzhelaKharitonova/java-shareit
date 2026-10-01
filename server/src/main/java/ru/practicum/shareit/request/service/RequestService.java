package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;

import java.awt.print.Pageable;
import java.util.List;

@Service
public interface RequestService {
    RequestResponseDto createRequest(Long userId, RequestCreateDto requestCreateDto);

    List<RequestResponseDto> findRequestsByUser(Long userId);

    List<RequestResponseDto> findAllRequests(Long userId, Integer from, Integer size);

    RequestResponseDto findRequestById(Long requestId);

    void enrichItemsToRequest(List<RequestResponseDto> dtoList);
}
