package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.request.dto.RequestCreateDto;
import ru.practicum.shareit.util.Constants;


@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody RequestCreateDto requestCreateDto) {
        log.info("Creating request {}, userId={}", requestCreateDto, userId);
        return requestClient.createRequest(userId, requestCreateDto);
    }

    @GetMapping
    public ResponseEntity<Object> findRequestsByUser(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId) {
        log.info("Get requestsByUser, userId={}", userId);
        return requestClient.findRequestsByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequests(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Get requests from={}, size={}", from, size);
        return requestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findRequestById(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("requestId") Long requestId) {
        return requestClient.findRequestById(userId, requestId);
    }
}
