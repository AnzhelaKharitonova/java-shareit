package ru.practicum.shareit.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.request.dto.RequestResponseDto;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto createRequest(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @RequestBody RequestCreateDto requestCreateDto) {
        return requestService.createRequest(userId, requestCreateDto);
    }

    @GetMapping
    public List<RequestResponseDto> findRequestsByUser(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId) {
        return requestService.findRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<RequestResponseDto> findAllRequests(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader(Constants.USER_ID_HEADER) Long userId) {
        return requestService.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto findRequestById(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("requestId") long requestId) {
        return requestService.findRequestById(requestId);
    }

}
