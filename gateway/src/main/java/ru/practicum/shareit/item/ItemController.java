package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.util.Constants;

import java.util.List;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;


    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto) {
        log.info("Creating item {}, userId={}", itemCreateDto, userId);
        return itemClient.createItem(itemCreateDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("itemId") long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto) {
        log.info("Updating itemId={}, userId={}, item {}", itemId, userId, itemUpdateDto);
        return itemClient.updateItem(itemId, userId, itemUpdateDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@PathVariable("itemId") Long itemId) {
        log.info("Get item, itemId={}", itemId);
        return itemClient.findItemById(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> findItemsByOwner(@RequestHeader(Constants.USER_ID_HEADER) Long ownerId) {
        log.info("Get itemsByOwner, ownerId={}", ownerId);
        return itemClient.findItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam String text) {
        if (text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        log.info("Search items, text {}", text);
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(Constants.USER_ID_HEADER) Long userId,
            @PathVariable("itemId") Long itemId,
            @Valid @RequestBody CommentDto commentDto) {
        log.info("Creating comment {}, itemId={}, userId={}", commentDto, itemId, userId);

        return itemClient.addComment(userId, itemId, commentDto);
    }

}
